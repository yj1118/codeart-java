package com.apros.codeart.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.Activator;

public final class PrimitiveUtil {
	private PrimitiveUtil() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T convertT(Object value, Class<T> targetType) {
		return (T) convert(value, targetType);
	}

	public static Object convert(Object value, Class<?> targetType) {
		if (value == null) {
			return getDefaultValue(targetType);
		}
		if (targetType.isInstance(value)) {
			return value;
		}
		if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
			return Integer.valueOf(value.toString());
		} else if (targetType.equals(long.class) || targetType.equals(Long.class)) {
			return Long.valueOf(value.toString());
		} else if (targetType.equals(UUID.class)) {
			return UUID.fromString(value.toString());
		} else if (targetType.equals(double.class) || targetType.equals(Double.class)) {
			return Double.valueOf(value.toString());
		} else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
			return Boolean.valueOf(value.toString());
		} else if (targetType.equals(float.class) || targetType.equals(Float.class)) {
			return Float.valueOf(value.toString());
		} else if (targetType.equals(byte.class) || targetType.equals(Byte.class)) {
			return Byte.valueOf(value.toString());
		} else if (targetType.equals(short.class) || targetType.equals(Short.class)) {
			return Short.valueOf(value.toString());
		} else if (targetType.equals(LocalDateTime.class)) {
			return ISO8601.getLocalDateTime(value.toString());
		} else if (targetType.equals(Instant.class)) {
			return ISO8601.getInstant(value.toString());
		} else if (targetType.equals(char.class) || targetType.equals(Character.class)) {
			return (Character) value.toString().charAt(0);
		} else if (targetType.equals(String.class)) {
			return value.toString();
		}

		throw new IllegalArgumentException(
				Language.strings("codeart", "CannotConvert", value.toString(), targetType.getSimpleName()));
	}

	private static Function<Class<?>, Object> _getDefaultValue = LazyIndexer.init((valueType) -> {
		return valueType.isPrimitive() ? Activator.createInstance(valueType) : null;
	});

	public static Object getDefaultValue(Class<?> valueType) {
		return _getDefaultValue.apply(valueType);
	}

	/**
	 * 
	 * 获得基类型的类型
	 * 
	 * 注意，我们把时间作为基础类型处理
	 * 
	 * @param typeName
	 * @return
	 */
	public static Class<?> getType(String typeName) {
		switch (typeName.toLowerCase()) {
		case "char":
			return char.class;
		case "bool":
		case "boolean":
			return boolean.class;
		case "byte":
			return byte.class;
		case "datetime":
			return LocalDateTime.class;
		case "double":
			return double.class;
		case "short":
			return short.class;
		case "int":
			return int.class;
		case "single":
		case "long":
			return long.class;
		case "float":
			return float.class;
		case "string":
			return String.class;
		case "guid":
			return UUID.class;
		default:
			return null;
		}
	}

	/**
	 * 注意，我们把时间作为基础类型处理
	 * 
	 * @param valueType
	 * @return
	 */
	public static boolean is(Class<?> valueType) {

		if (valueType.isPrimitive() || valueType.equals(LocalDateTime.class) || valueType.equals(UUID.class))
			return true;
		return false;
	}

}
