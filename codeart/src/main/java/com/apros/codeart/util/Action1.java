package com.apros.codeart.util;

@FunctionalInterface
public interface Action1<T> {
	void apply(T t) throws Exception;
}
