package apros.codeart.util;

import java.util.Locale;

public final class localeUtil {

    private localeUtil() {
    }

    public static Locale get(String language) {
        Locale locale = Locale.ENGLISH;
        switch (language) {
            case "en-US":
            case "en":
                locale = Locale.ENGLISH;
                break;
            case "ja-JP":
            case "ja":
                locale = Locale.JAPANESE;
                break;
            case "zh-TW":
                locale = Locale.TAIWAN;
                break;
            case "zh-CN":
            case "zh":
                locale = Locale.CHINESE;
                break;
        }

        return locale;
    }

}
