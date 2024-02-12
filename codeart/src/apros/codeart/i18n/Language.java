package apros.codeart.i18n;

import java.util.ResourceBundle;

public class Language {

	public static String strings(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle("Strings");
	    return bundle.getString(key);
	}
	
	public static String strings(String key, Object... args) {
		return String.format(strings(key), args);
	}
	
}
