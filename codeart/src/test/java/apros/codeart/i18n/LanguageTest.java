package apros.codeart.i18n;

import apros.codeart.core.TestRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

class LanguageTest {

    @Test
    void strings_cannot_empty() {
        Util.check_strings("CannotEmpty", "%s cannot be empty", "%s不能为空", "%sは空ではいけません");
        Util.check_strings("CannotEmpty", "a cannot be empty", "a不能为空", "aは空ではいけません", "a");
    }

}
