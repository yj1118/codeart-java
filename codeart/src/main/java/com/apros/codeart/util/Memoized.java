package com.apros.codeart.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示通过存储昂贵函数调用的结果，并在后续调用中返回缓存的结果来避免重复计算，
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Memoized {

}
