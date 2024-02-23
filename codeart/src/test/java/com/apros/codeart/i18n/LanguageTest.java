package com.apros.codeart.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.apros.codeart.TestRunner;

@ExtendWith(TestRunner.class)
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
		Util.check_strings("CannotEmpty", "%s cannot be empty", "%s不能为空", "%sは空ではいけません");
		Util.check_strings("CannotEmpty", "a cannot be empty", "a不能为空", "aは空ではいけません", "a");
	}

}
