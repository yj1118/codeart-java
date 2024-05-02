package apros.codeart.ddd.message;

import apros.codeart.ddd.message.internal.DomainMessagePublisher;
import apros.codeart.ddd.message.internal.MessageLog;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.Guid;

public final class DomainMessage {
	private DomainMessage() {
	}

	/**
	 * 
	 * 发送领域消息
	 * 
	 * @param name
	 * @param content
	 */
	public static void send(String name, DTObject content) {

		var id = Guid.compact();
		// 这里写日志
		MessageLog.write(id, name, content);

		// 挂载事件
		var publisher = new DomainMessagePublisher(id, name, content);
		var dataContext = DataContext.getCurrent();
		dataContext.committed().add(publisher);
	}

	/**
	 * 
	 * 订阅消息
	 * 
	 * @param name
	 * @param handler
	 */
	public static void subscribe(String name, DomainMessageHandler handler) {
		EventPortal.subscribe(name, handler);
	}

}
