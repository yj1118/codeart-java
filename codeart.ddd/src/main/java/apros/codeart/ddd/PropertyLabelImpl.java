package apros.codeart.ddd;

import java.util.Locale;
import java.util.function.Function;

import apros.codeart.context.AppSession;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;

class PropertyLabelImpl {

	public static String getValue(String name) {
		// 只有@前缀字符串才需要多语言支持
		if (!name.startsWith("@"))
			return name;
		return _getValue.apply(AppSession.locale()).apply(name);
	}

	private static Function<Locale, Function<String, String>> _getValue = LazyIndexer.init((local) -> {
		return LazyIndexer.init((name) -> {
			// null表示直接用资源根目录下的strings文件
			var value = name.substring(1);
			return Language.strings(local, null, value);
		});
	});
}
