package com.apros.codeart.runtime;

public final class TypeUtil {
	private TypeUtil() {
	}

	public static String resolveName(Class<?> cls) {
		return cls.getName(); // 先用自带的，后面升级为可以识别泛型的名称
	}

}
