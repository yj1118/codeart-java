package apros.codeart.ddd.message;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.DataContextEventArgs;
import apros.codeart.dto.DTObject;
import apros.codeart.util.Guid;
import apros.codeart.util.IEventObserver;

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
		var sender = new DomainMessageSender(id, name, content);
		var dataContext = DataContext.getCurrent();
		dataContext.committed().add(sender);

	}

	private static class DomainMessageSender implements IEventObserver<DataContextEventArgs> {

		private String _id;

		private String _name;

		private DTObject _content;

		public DomainMessageSender(String id, String name, DTObject content) {
			_id = id;
			_name = name;
			_content = content;
		}

		@Override
		public void handle(Object sender, DataContextEventArgs args) {
			// 在这里真实发布消息

			// 已发布，日志也可以标记为输出了
			MessageLog.flush(_id);
		}

	}
}
