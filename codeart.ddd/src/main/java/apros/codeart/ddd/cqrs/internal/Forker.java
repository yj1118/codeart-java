package apros.codeart.ddd.cqrs.internal;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.dto.DTObject;

public final class Forker {
	private Forker() {
	}

	public static boolean isEnabled() {
		return CQRSConfig.fock();
	}

	public static void dispatch(String sql, MapData data) {
		if (!CQRSConfig.fock())
			return;

		DTObject content = DTObject.editable();
		content.setString("sql", sql);
		if (data != null) {
			content.combineObject("data", data.asDTO());
		}

		DomainMessage.send(messageName, content);
	}

	public static final String messageName = "d:cqrs-fork";

}
