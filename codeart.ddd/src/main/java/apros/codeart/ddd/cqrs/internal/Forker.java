package apros.codeart.ddd.cqrs.internal;

import java.util.function.Function;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.dto.DTObject;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

public final class Forker {
	private Forker() {
	}

	public static boolean isEnabled(String aggregate) {
		return StringUtil.contains(CQRSConfig.master(), aggregate);
	}

	/**
	 * 
	 * 分发以执行sql为基础的数据构成
	 * 
	 * @param aggregate 聚合，slave可以根据聚合来订阅数据，一般聚合就是一个内聚根的类名
	 * @param sql
	 * @param data
	 */
	public static void dispatch(String aggregate, String sql, MapData data) {
		if (!isEnabled(aggregate))
			return;

		DTObject content = DTObject.editable();
		content.setByte("type", ForkType.DB.getValue());
		content.setString("agg", aggregate);
		content.setString("sql", sql);
		if (data != null) {
			content.combineObject("data", data.asDTO());
		}

		var eventName = getEventName(aggregate);
		DomainMessage.send(eventName, content);
	}

	public static void subscribe(String aggregate) {
		var eventName = getEventName(aggregate);
		DomainMessage.subscribe(eventName, ReceiveChangedHandler.instance);
	}

	private static Function<String, String> _getEventName = LazyIndexer.init((aggregate) -> {
		return String.format("d:cqrs-fork-%s", aggregate);
	});

	public static String getEventName(String aggregate) {
		return _getEventName.apply(aggregate);
	}

}
