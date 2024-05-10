package apros.codeart.mq;

import org.apache.logging.log4j.util.Strings;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;

public final class MQConfig {
	private MQConfig() {
	}

	private static class Holder {

		private static final DTObject Section;

		private static final String Provider;

		static {

			var section = AppConfig.section("mq");
			if (section != null) {
				Provider = section.getString("provider", Strings.EMPTY);
			} else {
				section = DTObject.Empty;
				Provider = Strings.EMPTY;
			}

			Section = section;
		}
	}

	public static DTObject section() {
		return Holder.Section;
	}

	public static String provider() {
		return Holder.Provider;
	}

}
