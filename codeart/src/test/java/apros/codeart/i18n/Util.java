package apros.codeart.i18n;

import static apros.codeart.i18n.Language.strings;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

public class Util {
	public static void check_strings(String key, String en, String zh, String ja) {

		var current = Locale.getDefault();

		// 两种区域设置的英文
		{
			Locale.setDefault(Locale.ENGLISH);
			assertEquals(en, strings("codeart", key));

			Locale.setDefault(Locale.US);
			assertEquals(en, strings("codeart", key));
		}

		// 两种区域设置的中文
		{
			Locale.setDefault(Locale.CHINESE);
			assertEquals(zh, strings("codeart", key));

			Locale.setDefault(Locale.CHINA);
			assertEquals(zh, strings("codeart", key));
		}

		// 两种区域设置的日文
		{
			Locale.setDefault(Locale.JAPANESE);
			assertEquals(ja, strings("codeart", key));

			Locale.setDefault(Locale.JAPAN);
			assertEquals(ja, strings("codeart", key));
		}

		// 测试默认的本地设置下，各种语言的指定读取是否正确
		{
			Locale.setDefault(current);
			assertEquals(en, strings(Locale.ENGLISH, "codeart", key));
			assertEquals(en, strings(Locale.US, "codeart", key));
			assertEquals(zh, strings(Locale.CHINESE, "codeart", key));
			assertEquals(zh, strings(Locale.CHINA, "codeart", key));
			assertEquals(ja, strings(Locale.JAPANESE, "codeart", key));
			assertEquals(ja, strings(Locale.JAPAN, "codeart", key));
		}
	}

	public static void check_strings(String key, String en, String zh, String ja, Object... args) {

		var current = Locale.getDefault();

		// 两种区域设置的英文
		{
			Locale.setDefault(Locale.ENGLISH);
			assertEquals(en, strings("codeart", key, args));

			Locale.setDefault(Locale.US);
			assertEquals(en, strings("codeart", key, args));
		}

		// 两种区域设置的中文
		{
			Locale.setDefault(Locale.CHINESE);
			assertEquals(zh, strings("codeart", key, args));

			Locale.setDefault(Locale.CHINA);
			assertEquals(zh, strings("codeart", key, args));
		}

		// 两种区域设置的日文
		{
			Locale.setDefault(Locale.JAPANESE);
			assertEquals(ja, strings("codeart", key, args));

			Locale.setDefault(Locale.JAPAN);
			assertEquals(ja, strings("codeart", key, args));
		}

		// 测试默认的本地设置下，各种语言的指定读取是否正确
		{
			Locale.setDefault(current);
			assertEquals(en, strings(Locale.ENGLISH, "codeart", key, args));
			assertEquals(en, strings(Locale.US, "codeart", key, args));
			assertEquals(zh, strings(Locale.CHINESE, "codeart", key, args));
			assertEquals(zh, strings(Locale.CHINA, "codeart", key, args));
			assertEquals(ja, strings(Locale.JAPANESE, "codeart", key, args));
			assertEquals(ja, strings(Locale.JAPAN, "codeart", key, args));
		}
	}
}
