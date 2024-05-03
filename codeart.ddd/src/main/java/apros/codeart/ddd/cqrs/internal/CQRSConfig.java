package apros.codeart.ddd.cqrs.internal;

import java.util.ArrayList;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;

public final class CQRSConfig {

	private CQRSConfig() {

	}

	private static ArrayList<Master> _masters;

	public static Iterable<Master> masters() {
		return _masters;
	}

	private static void init(DTObject config) {
		loadMaster(config);
	}

	@SuppressWarnings("unchecked")
	private static void loadMaster(DTObject config) {
		_masters = new ArrayList<Master>();

		config.each("master", (name, value) -> {
			var members = ListUtil.map((Iterable<DTObject>) value, (t) -> t.getString());
			_masters.add(new Master(name, members));
		});
	}

	private static DTObject _section;

	public static DTObject section() {
		return _section;
	}

	static {

		_section = AppConfig.section("cqrs");
		if (_section != null) {
			init(_section);
		} else {
			_section = DTObject.Empty;
			_masters = new ArrayList<Master>();
		}

	}
}
