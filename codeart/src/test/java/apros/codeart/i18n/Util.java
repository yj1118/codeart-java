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
			assertEquals(en, strings("apros.codeart", key));

			Locale.setDefault(Locale.US);
			assertEquals(en, strings("apros.codeart", key));
		}

		// 两种区域设置的中文
		{
			Locale.setDefault(Locale.CHINESE);
			assertEquals(zh, strings("apros.codeart", key));

			Locale.setDefault(Locale.CHINA);
			assertEquals(zh, strings("apros.codeart", key));
		}

		// 两种区域设置的日文
		{
			Locale.setDefault(Locale.JAPANESE);
			assertEquals(ja, strings("apros.codeart", key));

			Locale.setDefault(Locale.JAPAN);
			assertEquals(ja, strings("apros.codeart", key));
		}

		// 测试默认的本地设置下，各种语言的指定读取是否正确
		{
			Locale.setDefault(current);
			assertEquals(en, strings(Locale.ENGLISH, "apros.codeart", key));
			assertEquals(en, strings(Locale.US, "apros.codeart", key));
			assertEquals(zh, strings(Locale.CHINESE, "apros.codeart", key));
			assertEquals(zh, strings(Locale.CHINA, "apros.codeart", key));
			assertEquals(ja, strings(Locale.JAPANESE, "apros.codeart", key));
			assertEquals(ja, strings(Locale.JAPAN, "apros.codeart", key));
		}
	}

	public static void check_strings(String key, String en, String zh, String ja, Object... args) {

		var current = Locale.getDefault();

		// 两种区域设置的英文
		{
			Locale.setDefault(Locale.ENGLISH);
			assertEquals(en, strings("apros.codeart", key, args));

			Locale.setDefault(Locale.US);
			assertEquals(en, strings("apros.codeart", key, args));
		}

		// 两种区域设置的中文
		{
			Locale.setDefault(Locale.CHINESE);
			assertEquals(zh, strings("apros.codeart", key, args));

			Locale.setDefault(Locale.CHINA);
			assertEquals(zh, strings("apros.codeart", key, args));
		}

		// 两种区域设置的日文
		{
			Locale.setDefault(Locale.JAPANESE);
			assertEquals(ja, strings("apros.codeart", key, args));

			Locale.setDefault(Locale.JAPAN);
			assertEquals(ja, strings("apros.codeart", key, args));
		}

		// 测试默认的本地设置下，各种语言的指定读取是否正确
		{
			Locale.setDefault(current);
			assertEquals(en, strings(Locale.ENGLISH, "apros.codeart", key, args));
			assertEquals(en, strings(Locale.US, "apros.codeart", key, args));
			assertEquals(zh, strings(Locale.CHINESE, "apros.codeart", key, args));
			assertEquals(zh, strings(Locale.CHINA, "apros.codeart", key, args));
			assertEquals(ja, strings(Locale.JAPANESE, "apros.codeart", key, args));
			assertEquals(ja, strings(Locale.JAPAN, "apros.codeart", key, args));
		}
	}
}
