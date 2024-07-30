package apros.codeart.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果需要多语言支持，字符串写 @ 前缀
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyLabel {
    /**
     * 如果需要多语言支持，字符串写 @ 前缀
     *
     * @return
     */
    String value();
}
