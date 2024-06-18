package apros.codeart.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@ExtendWith(TestRunner.class)
class StringTest {

    @Test
    void substr() {

        String str = "123456789";

        {
            var result = StringUtil.substr(str, 3);
            assertEquals("456789", result);
        }

        {
            var result = StringUtil.substr(str, 0);
            assertEquals("123456789", result);
        }

        {
            var result = StringUtil.substr(str, 8);
            assertEquals("9", result);
        }

        {
            var result = StringUtil.substr(str, 9);
            assertEquals("", result);
        }
    }

    @Test
    void substrLength() {

        String str = "123456789";

        {
            var result = StringUtil.substr(str, 0, 0);
            assertEquals("", result);
        }

        {
            var result = StringUtil.substr(str, 0, 3);
            assertEquals("123", result);
        }

        {
            var result = StringUtil.substr(str, 5, 3);
            assertEquals("678", result);
        }

        {
            var result = StringUtil.substr(str, 5, 13);
            assertEquals("6789", result);
        }

        {
            var result = StringUtil.substr(str, 8, 2);
            assertEquals("9", result);
        }

        {
            var result = StringUtil.substr(str, 15, 13);
            assertEquals("", result);
        }


    }


}
