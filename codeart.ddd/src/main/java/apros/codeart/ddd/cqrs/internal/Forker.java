package apros.codeart.ddd.cqrs.internal;

import java.util.function.Function;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.dto.DTObject;
import apros.codeart.util.LazyIndexer;

public final class Forker {
	private Forker() {
	}

	public static boolean isEnabled() {
		return CQRSConfig.master();
	}

	/**
	 * @param aggregate 聚合，slave可以根据聚合来订阅数据，一般聚合就是一个内聚根的类名
	 * @param sql
	 * @param data
	 */
	public static void dispatch(String aggregate, String sql, MapData data) {
		if (!CQRSConfig.master())
			return;

		DTObject content = DTObject.editable();
		content.setString("agg", aggregate);
		content.setString("sql", sql);
		if (data != null) {
			content.combineObject("data", data.asDTO());
		}

		var eventName = getEventName(aggregate);
		DomainMessage.send(eventName, content);
	}

	public static void subscribe(String aggregate) {
		// aggregate可以为 *,那么就是 d:cqrs-fork.*，可以截获所有的数据
		var eventName = getEventName(aggregate);
		DomainMessage.subscribe(eventName, ReceiveChangedHandler.instance);
	}

	private static Function<String, String> _getEventName = LazyIndexer.init((aggregate) -> {
		return String.format("d:cqrs-fork.%s", aggregate);
	});

	public static String getEventName(String aggregate) {
		return _getEventName.apply(aggregate);
	}

}
