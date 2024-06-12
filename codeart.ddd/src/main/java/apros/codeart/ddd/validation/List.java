package apros.codeart.ddd.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface List {

    int min() default 0;

    int max() default 0;

    /**
     * 是否检查子项
     *
     * @return true 检查，false不检查
     */
    boolean validateItem() default false;

}