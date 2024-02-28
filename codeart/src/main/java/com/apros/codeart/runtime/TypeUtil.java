package com.apros.codeart.runtime;

import com.google.common.reflect.TypeToken;

public final class TypeUtil {
	private TypeUtil() {
	}

	public static String resolveName(Class<?> cls) {
		return cls.getName(); // 先用自带的，后面升级为可以识别泛型的名称
	}

	/**
	 * 对于泛型集合，只能获得成员的上限类型
	 * 
	 * @param collectionType
	 * @return
	 */
	public static Class<?> resolveElementType(Class<?> collectionType) {

		if (collectionType.isArray()) {
			return collectionType.getComponentType();
		}

		TypeToken<?> typeToken = TypeToken.of(collectionType);

		// 获取集合的类型上限
		TypeToken<?> elementTypeToken = typeToken.resolveType(collectionType.getTypeParameters()[0]);

		// 获取集合的成员类型
		Class<?> elementType = elementTypeToken.getRawType();

		return elementType;
	}

}
