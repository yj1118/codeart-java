package test.codeart.i18n;

import static apros.codeart.i18n.Language.strings;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

public class Util {
	public static void check_strings(String key, String en, String zh, String ja) {

		var current = Locale.getDefault();

		// 两种区域设置的英文
		{
			Locale.setDefault(Locale.ENGLISH);
			assertEquals(en, strings(key));

			Locale.setDefault(Locale.US);
			assertEquals(en, strings(key));
		}

		// 两种区域设置的中文
		{
			Locale.setDefault(Locale.CHINESE);
			assertEquals(zh, strings(key));

			Locale.setDefault(Locale.CHINA);
			assertEquals(zh, strings(key));
		}

		// 两种区域设置的日文
		{
			Locale.setDefault(Locale.JAPANESE);
			assertEquals(ja, strings(key));

			Locale.setDefault(Locale.JAPAN);
			assertEquals(ja, strings(key));
		}

		// 测试默认的本地设置下，各种语言的指定读取是否正确
		{
			Locale.setDefault(current);
			assertEquals(en, strings(Locale.ENGLISH, key));
			assertEquals(en, strings(Locale.US, key));
			assertEquals(zh, strings(Locale.CHINESE, key));
			assertEquals(zh, strings(Locale.CHINA, key));
			assertEquals(ja, strings(Locale.JAPANESE, key));
			assertEquals(ja, strings(Locale.JAPAN, key));
		}
	}
}
