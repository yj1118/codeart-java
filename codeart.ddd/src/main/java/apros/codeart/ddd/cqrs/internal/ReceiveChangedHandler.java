//package apros.codeart.ddd.cqrs.internal;
//
//import apros.codeart.ddd.MapData;
//import apros.codeart.ddd.message.DomainMessageHandler;
//import apros.codeart.ddd.metadata.ObjectMetaLoader;
//import apros.codeart.ddd.repository.DataContext;
//import apros.codeart.dto.DTObject;
//import apros.codeart.mq.event.EventPriority;
//
//class ReceiveChangedHandler extends DomainMessageHandler {
//
//	private ReceiveChangedHandler() {
//	}
//
//	@Override
//	public EventPriority getPriority() {
//		return EventPriority.High;
//	}
//
//	@Override
//	public void process(String msgName, String msgId, DTObject content) {
//
//		var type = content.getByte("type");
//		var aggregate = content.getString("agg");
//
//		if (type == ForkType.DB.getValue()) {
//			forkDB(aggregate, content);
//			return;
//		}
//
//	}
//
//	private static void forkDB(String aggregate, DTObject content) {
//
//		if (ObjectMetaLoader.exists(aggregate)) {
//			// 如果本地有此聚合，那么表示订阅了*，把自己也订阅了，所以不再自动入库
//			return;
//		}
//
//		var branch = BranchFactory.create();
//
//		var store = true;
//		// 自动入库
//		var sql = content.getString("sql");
//
//		if (content.exist("data")) {
//			var data = new MapData();
//			content.each("data", (name, value) -> {
//				data.put(name, value);
//			});
//			DataContext.newScope((access) -> {
//				access.execute(sql, data);
//			});
//		} else {
//			DataContext.newScope((access) -> {
//				access.execute(sql);
//			});
//		}
//	}
//
//	public static final ReceiveChangedHandler instance = new ReceiveChangedHandler();
//
//}
