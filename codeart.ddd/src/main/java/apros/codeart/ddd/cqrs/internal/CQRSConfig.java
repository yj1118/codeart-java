package apros.codeart.ddd.cqrs.internal;

import java.util.ArrayList;

import apros.codeart.AppConfig;
import apros.codeart.ddd.metadata.MetadataLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;

public final class CQRSConfig {

	private CQRSConfig() {

	}

	private static ArrayList<String> _master;

	public static Iterable<String> master() {
		return _master;
	}

	private static void init(DTObject config) {
		loadMaster(config);
	}

	private static void loadMaster(DTObject config) {
		_master = new ArrayList<String>();

		var temp = config.getStrings("master");
		if (temp == null)
			return;

		for (var item : temp) {
			if (item.equalsIgnoreCase("*")) {
				for (var domainType : MetadataLoader.getDomainTypes()) {
					if (_master.contains(domainType.getSimpleName()))
						return;
					_master.add(domainType.getSimpleName());
				}
				break;
			}

			if (item.startsWith("-")) {
				var name = item.substring(1);
				ListUtil.removeFirst(_master, (t) -> t.equalsIgnoreCase(name));
				break;
			}
			
			
			if (_master.contains(item))
				return;
			_master.add(item);
		}

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
			_master = new ArrayList<String>();
		}

	}
}
