package apros.codeart.ddd.message.internal;

import java.util.List;
import java.util.UUID;

import apros.codeart.ddd.message.MessageEntry;
import apros.codeart.ddd.message.MessageLogFactory;
import apros.codeart.dto.DTObject;

public final class MessageLog {
    private MessageLog() {
    }

    public static void write(String id, String name, DTObject content) {
        // 写入日志之前，先写入同步信息
        AtomicOperation.insert(id);

        var logger = MessageLogFactory.createLog();
        logger.write(id, name, content);
    }

    public static void flush(String id) {
        var logger = MessageLogFactory.createLog();
        logger.flush(id);

        // 删除日志后，删除同步信息
        AtomicOperation.delete(id);
    }

    public static MessageEntry find(String id) {
        var logger = MessageLogFactory.createLog();
        return logger.find(id);
    }

    /**
     * 找到由于中断的原因要发送消息的编号
     *
     * @return
     */
    public static List<String> findInterrupteds() {
        return AtomicOperation.findInterrupteds();
    }

    /**
     * 清理废弃的资源
     */
    public static void cleanup() {
        var logger = MessageLogFactory.createLog();
        logger.cleanup();
    }

}
