package com.apros.codeart.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public final class Language {

	private Language() {
	}

	public static String strings(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle("strings");
		return bundle.getString(key);
	}

	public static String strings(Locale locale, String key) {
		ResourceBundle bundle = ResourceBundle.getBundle("strings", locale);
		return bundle.getString(key);
	}

	public static String strings(String key, Object... args) {
		return String.format(strings(key), args);
	}

	public static String strings(Locale locale, String key, Object... args) {
		return String.format(strings(locale, key), args);
	}

}