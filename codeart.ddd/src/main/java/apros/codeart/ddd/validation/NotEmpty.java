package apros.codeart.ddd.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotEmpty {
	/**
	 * 
	 * 是否过滤空格，默认情况下过滤
	 * 
	 * @return
	 */
	boolean trim() default true;
}
