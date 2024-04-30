package apros.codeart.ddd.saga;

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

	private static String getQueueFolder(String queueId) {
		return IOUtil.combine(_rootFolder, queueId);
	}

	@Override
	public void writeRaiseStart(String queueId) {
		init(queueId);
		IOUtil.createDirectory(_folder);
	}

	@Override
	public void writeRaise(String queueId, String eventName) {
		_raisePointer++;
		var fileName = IOUtil.combine(_folder, String.format("%02d.%s.d", _raisePointer, eventName));
		IOUtil.atomicNewFile(fileName);
	}

	@Override
	public void writeRaiseLog(String queueId, String eventName, DTObject log) {
		var fileName = IOUtil.combine(_folder, String.format("%02d.%s-log.d", _raisePointer, eventName));
		IOUtil.atomicWrite(fileName, log.getCode());
	}

	@Override
	public void writeRaiseEnd(String queueId) {
		var fileName = IOUtil.combine(_folder, "end");
		IOUtil.atomicNewFile(fileName);
	}

	@Override
	public void writeReverseStart(String queueId) {
		init(queueId);
	}

	@Override
	public List<RaisedEntry> findRaised(String queueId) {
		var items = new ArrayList<RaisedEntry>();

		return items;
	}

	@Override
	public void writeReversed(RaisedEntry entry) {
		// 删除回溯文件，就表示回溯完毕了
		var eventFile = IOUtil.combine(_folder, String.format("%02d.%s.d", entry.index(), entry.name()));
		var logFile = IOUtil.combine(_folder, String.format("%02d.%s-log.d", _raisePointer, entry.name()));
		IOUtil.delete(eventFile);
		IOUtil.delete(logFile);
	}

	@Override
	public void writeReverseEnd(String queueId) {
		init(queueId);
		IOUtil.delete(_folder);
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
