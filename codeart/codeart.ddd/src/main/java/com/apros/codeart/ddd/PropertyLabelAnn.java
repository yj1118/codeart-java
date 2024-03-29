package com.apros.codeart.ddd;

import java.util.Locale;
import java.util.function.Function;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.LazyIndexer;

class PropertyLabelAnn {

	public static String getValue(String name) {
		return _getValue.apply(ContextSession.locale()).apply(name);
	}

	private static Function<Locale, Function<String, String>> _getValue = LazyIndexer.init((local) -> {
		return LazyIndexer.init((name) -> {
			return Language.strings(local, name);
		});
	});
}
