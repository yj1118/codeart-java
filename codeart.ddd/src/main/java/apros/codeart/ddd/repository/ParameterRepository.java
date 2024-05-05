package apros.codeart.ddd.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParameterRepository {
	String loadMethod();

	/**
	 * 参数有可能是抽象的，这种情况下，我们有可能需要制定参数在仓储创建时使用的类型
	 */
	Class<?> implementType();
}
