package apros.codeart.ddd.cqrs.internal;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.message.DomainMessageHandler;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPriority;

class ReceiveChangedHandler extends DomainMessageHandler {

	private ReceiveChangedHandler() {
	}

	@Override
	public EventPriority getPriority() {
		return EventPriority.High;
	}

	@Override
	public void process(String msgName, String msgId, DTObject content) {

		var aggregate = content.getString("agg");

		// 如果本地有此聚合，那么表示订阅了*，把自己也订阅了，所以不处理
		if (ObjectMetaLoader.exists(aggregate))
			return;

		var sql = content.getString("sql");

		if (content.exist("data")) {
			var data = new MapData();
			content.each("data", (name, value) -> {
				data.put(name, value);
			});
			DataContext.newScope((access) -> {
				access.execute(sql, data);
			});
		} else {
			DataContext.newScope((access) -> {
				access.execute(sql);
			});
		}

		// 分发处理handler，用户注册了就处理，否则不处理
		todo
	}

	public static final ReceiveChangedHandler instance = new ReceiveChangedHandler();

}
