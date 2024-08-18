package apros.codeart.ddd.message;

import apros.codeart.ddd.message.internal.MessageFilter;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.event.IEventHandler;

public abstract class DomainMessageHandler implements IEventHandler {

    @Override
    public void handle(String eventName, DTObject data) {
        var msgId = data.getString("id");
        var content = data.getObject("body");

        DataContext.using(() -> {
            // 保持幂等性
            if (MessageFilter.exist(msgId)) return;

            handle(content);
            //能够保存编号，就可以提交，编号是唯一的，如果重复插入会报错
            MessageFilter.insert(msgId);
        });
    }

    protected abstract void handle(DTObject content);

}
