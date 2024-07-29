package apros.codeart.dto.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于为构造函数追加dto更多的适配名称
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DTOParameter {
    String[] value(); // 定义接收字符串数组的参数
}
