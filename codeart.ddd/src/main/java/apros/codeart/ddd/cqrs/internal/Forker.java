package apros.codeart.ddd.cqrs.internal;

import static apros.codeart.i18n.Language.strings;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;

public final class Forker {
	private Forker() {
	}

	private static Master findMaster(String aggregate) {
		return ListUtil.find(CQRSConfig.masters(), (m) -> {
			return m.name().equalsIgnoreCase(aggregate);
		});
	}

	private static Master findMaster(Object obj) {
		var aggregate = obj.getClass().getSimpleName();
		return findMaster(aggregate);
	}

	public static boolean isEnabled(Object obj) {
		return findMaster(obj) != null;
	}

	public static boolean isEnabled(String aggregate) {
		return findMaster(aggregate) != null;
	}

	private static DTObject getData(IAggregateRoot root) {
		return DomainObject.getData((DomainObject) root, (obj) -> {
			var master = findMaster(obj);
			if (master == null)
				throw new IllegalStateException(strings("codeart.ddd", "NoMaster", obj.getClass().getSimpleName()));

			return master.members();
		});
	}

	public static void notifyAdd(IAggregateRoot root) {
		if (!isEnabled(root))
			return;

		var data = getData(root);

	}

	public static void notifyUpdate(IAggregateRoot root) {
		if (!isEnabled(root))
			return;

		var data = getData(root);

	}

	public static void notifyDelete(IAggregateRoot root) {
		if (!isEnabled(root))
			return;

		var id = root.getIdentity();
	}

//	/**
//	 * 
//	 * 分发以执行sql为基础的数据构成
//	 * 
//	 * @param aggregate 聚合，slave可以根据聚合来订阅数据，一般聚合就是一个内聚根的类名
//	 * @param sql
//	 * @param data
//	 */
//	public static void dispatch(String aggregate, String sql, MapData data) {
//		if (!isEnabled(aggregate))
//			return;
//
//		DTObject content = DTObject.editable();
//		content.setByte("type", ForkType.DB.getValue());
//		content.setString("agg", aggregate);
//		content.setString("sql", sql);
//		if (data != null) {
//			content.combineObject("data", data.asDTO());
//		}
//
//		var eventName = getEventName(aggregate);
//		DomainMessage.send(eventName, content);
//	}
//
//	public static void subscribe(String aggregate) {
//		var eventName = getEventName(aggregate);
//		DomainMessage.subscribe(eventName, ReceiveChangedHandler.instance);
//	}
//
//	private static Function<String, String> _getEventName = LazyIndexer.init((aggregate) -> {
//		return String.format("d:cqrs-fork-%s", aggregate);
//	});
//
//	public static String getEventName(String aggregate) {
//		return _getEventName.apply(aggregate);
//	}

}
