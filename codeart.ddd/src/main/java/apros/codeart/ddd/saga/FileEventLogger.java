package apros.codeart.ddd.saga;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;

final class FileEventLogger implements IEventLog {

	private String _folder;

	private int _raisePointer;

	private void init(String queueId) {
		if (_folder == null) {
			_folder = getQueueFolder(queueId);
		}
	}

	FileEventLogger() {
		_raisePointer = -1;
	}

	@Override
	public void writeRaiseStart(String queueId) {
		init(queueId);
		IOUtil.createDirectory(_folder);
	}

	@Override
	public void writeRaise(String queueId, String eventName) {
		_raisePointer++;
		var fileName = getEventFileName(_folder, _raisePointer, eventName);
		IOUtil.atomicNewFile(fileName);
	}

	@Override
	public void writeRaiseLog(String queueId, String eventName, DTObject log) {
		var fileName = getEventLogFileName(_folder, _raisePointer, eventName);
		IOUtil.atomicWrite(fileName, log.getCode());
	}

	@Override
	public void writeRaiseEnd(String queueId) {
		var fileName = getEndFileName(_folder);
		IOUtil.atomicNewFile(fileName);
	}

	@Override
	public void writeReverseStart(String queueId) {
		init(queueId);
	}

	@Override
	public List<RaisedEntry> findRaised(String queueId) {
		var items = new ArrayList<RaisedEntry>();

		IOUtil.search(_folder, "*.{e}", (file) -> {
			var name = file.getFileName().toString();
			var temp = name.split(".");
			var index = Integer.parseInt(temp[0]);
			var eventName = temp[1];

			var logFileName = getEventLogFileName(_folder, index, eventName);
			var logCode = IOUtil.readString(logFileName);
			DTObject log = logCode == null ? DTObject.Empty : DTObject.readonly(logCode);
			items.add(new RaisedEntry(index, eventName, log));
		});

		return items;
	}

	@Override
	public void writeReversed(RaisedEntry entry) {
		// 删除回溯文件，就表示回溯完毕了
		var eventFile = getEventFileName(_folder, entry.index(), entry.name());
		IOUtil.delete(eventFile);
		var logFile = getEventLogFileName(_folder, entry.index(), entry.name());
		IOUtil.delete(logFile);
	}

	@Override
	public void writeReverseEnd(String queueId) {
		init(queueId);
		IOUtil.delete(_folder);
	}

	@Override
	public List<String> findInterrupteds(int top) {
		var items = new ArrayList<String>(top);
		IOUtil.eachFolder(_rootFolder, (folder) -> {
			Path endPath = folder.resolve("end");
			if (!Files.exists(endPath)) {
				// 没有end文件，表明被中断了
				var queueId = folder.getFileName().toString();
				items.add(queueId);
			}
		});
		return items;
	}

	private static String getQueueFolder(String queueId) {
		return IOUtil.combine(_rootFolder, queueId);
	}

	private static String getEventFileName(String folder, int index, String eventName) {
		return IOUtil.combine(folder, String.format("%02d.%s.e", index, eventName));
	}

	private static String getEventLogFileName(String folder, int index, String eventName) {
		return IOUtil.combine(folder, String.format("%02d.%s.l", index, eventName));
	}

	private static String getEndFileName(String folder) {
		return IOUtil.combine(folder, "end");
	}

	private static final String _rootFolder;

	static {
		var config = AppConfig.section("saga");
		String folder = null;
		if (config != null) {
			folder = config.getString("folder", null);
		}

		_rootFolder = folder == null ? IOUtil.getCurrentDirectory() : folder;
	}

}
