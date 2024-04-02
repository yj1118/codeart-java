package com.apros.codeart.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只有标记了该特性的领域属性，ORM才会存到仓储中
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyRepository {

	/**
	 * 
	 * 可以使用自定义方法加载该属性，请保证加载属性的方法是数据仓储的静态成员
	 * 
	 * 该方法会在懒惰加载中用到
	 * 
	 * 如果属性在仓储构造函数中出现，那么也会用到该方法
	 * 
	 * @return
	 */
	String loadMethod() default "";

	/**
	 * 指示属性是否为懒惰加载，即：当用到时才加载
	 * 
	 * @return
	 */
	boolean lazy() default false;
}
