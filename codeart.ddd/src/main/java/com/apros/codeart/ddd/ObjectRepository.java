package com.apros.codeart.ddd;

/**
 * 表示对象可以仓储，仓储的接口类型为所在聚合根的仓储的类型
 */
public @interface ObjectRepository {

	Class<?> repositoryInterfaceType();

	boolean closeMultiTenancy() default true;
}
