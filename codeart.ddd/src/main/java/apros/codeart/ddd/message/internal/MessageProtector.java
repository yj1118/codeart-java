package apros.codeart.ddd.message.internal;

import apros.codeart.context.AppSession;
import apros.codeart.log.Logger;

public final class MessageProtector {
    private MessageProtector() {

    }

    private static void tryContinue(String msgId) {

        var msg = MessageLog.find(msgId);

        // 为null表示没有找到相关文件
        if (msg == null)
            return;

        DomainMessagePublisher.publish(msg.name(), msg.id(), msg.content());
    }

    /**
     * 尝试继续发送消息，这主要是为了防止系统崩溃/断电等问题导致的消息发送失败
     */
    private static void tryContinue() {
        try {

            var msgIds = MessageLog.findInterrupteds();

            if (msgIds == null || msgIds.size() == 0)
                return;

            msgIds.parallelStream().forEach(msgId -> {
                AppSession.using(() -> {
                    tryContinue(msgId);
                });
            });

        } catch (Throwable ex) {
            Logger.error(ex);
        }
    }

    private static void cleanup() {
        MessageLog.cleanup();
    }

    /**
     * 启动保护器，会尝试继续发送消息和清理垃圾信息
     */
    public static void launch() {
        tryContinue();
        cleanup();
    }

}
