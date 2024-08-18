package apros.codeart.ddd.message;

import apros.codeart.ddd.message.internal.DomainMessagePublisher;
import apros.codeart.ddd.message.internal.MessageLog;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.util.Guid;

public final class DomainMessage {
    private DomainMessage() {
    }

    /**
     * 发送领域消息
     *
     * @param name
     * @param content
     */
    public static void send(String messageName, DTObject content) {

        var id = Guid.compact();
        // 这里写日志
        MessageLog.write(id, messageName, content);

        // 挂载事件
        var publisher = new DomainMessagePublisher(id, messageName, content);
        var dataContext = DataContext.getCurrent();
        //提交成功之后，会执行publisher的handle方法
        dataContext.committed().add(publisher);
    }

    /**
     * 订阅消息
     *
     * @param name
     * @param handler
     */
    public static void subscribe(String messageName, DomainMessageHandler handler) {
        EventPortal.subscribe(messageName, handler, true);
    }

    public static void cancel(String messageName) {
        EventPortal.cancel(messageName);
    }

}
