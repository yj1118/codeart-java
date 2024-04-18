package apros.codeart.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 指示对象是个领域派生类，这个注解主要用于领域对象多态的情况，标记类的编号
 * 
 * 因为如果数据库直接存类的路径，万一以后重构代码，类名改变了，就无法动态创建对象了
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DerivedClass {

	String value();

}
