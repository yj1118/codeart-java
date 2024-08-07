package apros.codeart.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import apros.codeart.context.AppSession;
import apros.codeart.util.StringUtil;

public final class Language {

    private Language() {
    }

    public static String strings(String baseName, String key) {
        var name = StringUtil.isNullOrEmpty(baseName) ? "strings" : String.format("%s.strings", baseName);
        ResourceBundle bundle = ResourceBundle.getBundle(name, AppSession.locale());
        return bundle.getString(key);
    }

    public static String strings(Locale locale, String baseName, String key) {
        var name = StringUtil.isNullOrEmpty(baseName) ? "strings" : String.format("%s.strings", baseName);
        ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
        return bundle.getString(key);
    }

    public static String stringsMessageFormat(String baseName, String key, Object... args) {
        return StringUtil.format(strings(baseName, key), args);
    }

    public static String strings(String baseName, String key, Object... args) {
        return String.format(strings(baseName, key), args);
    }

    public static String strings(Locale locale, String baseName, String key, Object... args) {
        return String.format(strings(locale, baseName, key), args);
    }

    //region 专用于客户项目获取多语言字符串的方法

    public static String get(String key) {
        return strings(null, key);
    }

    public static String get(String key, Object... args) {
        return strings(null, key, args);
    }

    //endregion

}