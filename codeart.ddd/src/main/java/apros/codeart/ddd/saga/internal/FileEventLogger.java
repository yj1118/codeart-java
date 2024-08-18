package apros.codeart.ddd.saga.internal;

import static apros.codeart.runtime.Util.propagate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import apros.codeart.ddd.saga.IEventLog;
import apros.codeart.ddd.saga.RaisedEntry;
import apros.codeart.ddd.saga.SAGAConfig;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;
import apros.codeart.util.Guid;
import apros.codeart.util.StringUtil;
import apros.codeart.util.TimeUtil;

final class FileEventLogger implements IEventLog {

    private String _folder;

    private void init(String queueId) {
        if (_folder == null) {
            _folder = getQueueFolder(queueId);
        }
    }

    FileEventLogger() {
    }

    @Override
    public String newId() {
        var day = TimeUtil.format(Instant.now(), "yyyyMMdd");
        return String.format(day, Guid.compact());
    }

    @Override
    public void writeRaiseStart(String queueId) {
        init(queueId);
        IOUtil.createDirectory(_folder);
    }

    @Override
    public void writeRaise(String queueId, String eventName, int entryIndex) {
        var fileName = getEventFileName(_folder, entryIndex, eventName);
        IOUtil.atomicNewFile(fileName);
    }

    @Override
    public void writeRaiseLog(String queueId, String eventName, int entryIndex, DTObject log) {
        var fileName = getEventLogFileName(_folder, entryIndex, eventName);
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

    /**
     * 按照日期倒序访问目录
     *
     * @param action
     */
    private static void eachLastTwoDay(Consumer<Path> action) {

        Path rootDirectory = Paths.get(_rootFolder); // 替换为你的根目录路径

        try (Stream<Path> paths = Files.list(rootDirectory)) {
            List<Path> topDirectories = paths.filter(Files::isDirectory) // 确保是目录
                    .sorted(Comparator.comparing(Path::getFileName).reversed()) // 按目录名倒序排序
                    .limit(2) // 限制为前2个，也就是最近2天的数据
                    .collect(Collectors.toList()); // 收集结果到列表

            // 输出结果
            topDirectories.forEach(dir -> action.accept(dir));
        } catch (IOException e) {
            throw propagate(e);
        }

    }

    @Override
    public List<String> findInterrupteds() {
        var items = new ArrayList<String>();

        // 由于中断（断电/系统故障/硬件故障）发生后，系统就停止了运行，所以只用看最近2天的数据哪些需要恢复
        // 因为数据不恢复，就不会接受新的事件
        eachLastTwoDay((dir) -> {

            // dir是实际存放队列目录的文件夹
            IOUtil.eachFolder(dir, (folder) -> {
                // folder 是队列目录
                if (!isEnd(folder)) {
                    // 没有end文件，表明被中断了
                    var queueId = folder.getFileName().toString();
                    items.add(queueId);
                }
                return true;
            });

        });

        return items;
    }

    private static boolean isEnd(Path folder) {
        Path endPath = folder.resolve("end");
        return Files.exists(endPath);
    }

    @Override
    public void clean() {
        // 清理过期了的日志文件
        if (SAGAConfig.retainDays() <= 0)
            return; // 永久保留，不需要清理

        eachRetainDaysBefore(SAGAConfig.retainDays(), (dir) -> {
            // dir是实际存放队列目录的文件夹
            IOUtil.eachFolder(dir, (folder) -> {
                // folder 是队列目录
                if (isEnd(folder)) { // 只有队列执行结束了的队列才删除
                    // 安全的删除队列目录
                    safeDeleteQueueFolder(folder);
                }
                return true;
            });

            // 如果日期目录没有子文件或目录了，那么删除
            if (!IOUtil.hasSub(dir)) {
                IOUtil.delete(dir);
            }

        });
    }

    private static void safeDeleteQueueFolder(Path folder) {
        // 先删除除end文件之外的所有文件，防止中途中断导致下次进来无法识别队列是否结束的问题

        // 使用try-with-resources确保流在结束时关闭
        try (Stream<Path> stream = Files.list(folder)) {
            stream.filter(Files::isRegularFile) // 确保只处理文件
                    .filter(path -> !path.getFileName().toString().equals("end")) // 过滤掉文件名为"end"的文件
                    .forEach(path -> {
                        try {
                            Files.delete(path); // 删除文件

                        } catch (IOException e) {
                            throw propagate(e);
                        }
                    });
        } catch (IOException e) {
            throw propagate(e);
        }

        // 最后删除end
        IOUtil.delete(IOUtil.combine(folder, "end"));
    }

    private static void eachRetainDaysBefore(int days, Consumer<Path> action) {
        Path rootDirectory = Paths.get(_rootFolder);
        LocalDate daysAgo = LocalDate.now().minusDays(days);

        try (Stream<Path> paths = Files.list(rootDirectory)) {
            paths.filter(Files::isDirectory) // 确保只处理目录
                    .forEach(path -> {
                        try {
                            // 获取目录的创建时间
                            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                            LocalDate creationDate = attrs.creationTime().toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate();

                            // 判断创建时间是否在指定的时间
                            if (creationDate.isBefore(daysAgo)) {
                                action.accept(path);
                            }
                        } catch (IOException e) {
                            throw propagate(e);
                        }
                    });
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private static String getQueueFolder(String queueId) {
        // 前8位是创建的日期，以日期建立子目录
        return IOUtil.combine(_rootFolder, StringUtil.substr(queueId, 0, 8), queueId).toAbsolutePath().toString();
    }

    private static String getEventFileName(String folder, int index, String eventName) {
        return IOUtil.combine(folder, String.format("%02d.%s.e", index, eventName)).toAbsolutePath().toString();
    }

    private static String getEventLogFileName(String folder, int index, String eventName) {
        return IOUtil.combine(folder, String.format("%02d.%s.l", index, eventName)).toAbsolutePath().toString();
    }

    private static String getEndFileName(String folder) {
        return IOUtil.combine(folder, "end").toAbsolutePath().toString();
    }

    private static final String _rootFolder;

    static {
        String folder = SAGAConfig.section().getString("@log.folder", null);

        _rootFolder = folder == null
                ? IOUtil.combine(IOUtil.getCurrentDirectory(), "domain-event-log").toAbsolutePath().toString()
                : folder;

        IOUtil.createDirectory(_rootFolder);

    }

}
