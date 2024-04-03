package com.apros.codeart.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 定义注解在运行时保持可用
@Target(ElementType.CONSTRUCTOR) // 定义注解可用于构造函数
public @interface ConstructorRepository {

}
