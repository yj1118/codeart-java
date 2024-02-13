package test.codeart.i18n;

import org.junit.jupiter.api.Test;

class LanguageTest {

//	@Test
//	void readStrings() {
//		Locale englishUSLocale = Locale.ENGLISH;
//		Locale.setDefault(englishUSLocale);
//
//		var message = strings("cannot_empty");
//		assertEquals("%s cannot be empty", message);
//	}

	@Test
	void strings_cannot_empty() {
		Util.check_strings("cannot_empty", "%s cannot be empty", "%s不能为空", "%sは空ではいけません");
	}

}
