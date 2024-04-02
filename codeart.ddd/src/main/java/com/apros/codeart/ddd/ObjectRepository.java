package com.apros.codeart.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示对象可以仓储，仓储的接口类型为所在聚合根的仓储的类型
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectRepository {

	// repositoryInterfaceType
	Class<?> value() default Object.class;

	boolean closeMultiTenancy() default true;
}
