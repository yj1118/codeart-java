package apros.codeart.ddd;

import java.util.Locale;
import java.util.function.Function;

import apros.codeart.context.AppSession;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;

class PropertyLabelAnn {

	public static String getValue(String name) {
		return _getValue.apply(AppSession.locale()).apply(name);
	}

	private static Function<Locale, Function<String, String>> _getValue = LazyIndexer.init((local) -> {
		return LazyIndexer.init((name) -> {
			return Language.strings(local, "codeart.ddd", name);
		});
	});
}
