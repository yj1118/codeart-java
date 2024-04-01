package com.apros.codeart.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import com.apros.codeart.util.StringUtil;

public final class Language {

	private Language() {
	}

	public static String strings(String baseName, String key) {
		var name = StringUtil.isNullOrEmpty(baseName) ? "strings" : String.format("%s.strings", baseName);
		ResourceBundle bundle = ResourceBundle.getBundle(name);
		return bundle.getString(key);
	}

	public static String strings(Locale locale, String baseName, String key) {
		var name = StringUtil.isNullOrEmpty(baseName) ? "strings" : String.format("%s.strings", baseName);
		ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
		return bundle.getString(key);
	}

	public static String strings(String baseName, String key, Object... args) {
		return String.format(strings(baseName, key), args);
	}

	public static String strings(Locale locale, String baseName, String key, Object... args) {
		return String.format(strings(locale, baseName, key), args);
	}

	public static String strings(String key) {
		return strings(null, key);
	}

	public static String strings(String key, Object... args) {
		return strings(null, key, args);
	}

}