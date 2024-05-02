package apros.codeart.ddd.message.internal;

import apros.codeart.ddd.repository.DataContextEventArgs;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.IEventObserver;

public final class DomainMessagePublisher implements IEventObserver<DataContextEventArgs> {
	private String _id;

	private String _name;

	private DTObject _content;

	public DomainMessagePublisher(String id, String name, DTObject content) {
		_id = id;
		_name = name;
		_content = content;
	}

	@Override
	public void handle(Object sender, DataContextEventArgs args) {
		publish(_name, _id, _content);
	}

	public static void publish(String msgName, String msgId, DTObject content) {
		// 在这里真实发布消息
		var data = DTObject.editable();
		// 消息编号，可以用于幂等性计算
//		data.setBoolean(headerType, true); // 标记为领域消息
		data.setString("id", msgId);
		data.combineObject("body", content);

		EventPortal.publish(msgName, data);

		// 已发布，日志也可以标记为输出了
		MessageLog.flush(msgId);
	}

//	public static final String headerType = "__dm__";

}
