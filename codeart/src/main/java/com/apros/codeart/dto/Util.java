package com.apros.codeart.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;

final class Util {
	private Util() {
	}

	public static <T> AbstractList<T> createList(boolean isReadOnly, int count) {
		return isReadOnly ? new ArrayList<T>(count) : new LinkedList<T>();
	}

	public static boolean valueCodeIsString(Object value) {
		var cls = value.getClass();

		if (cls.equals(char.class))
			return true;

		if (cls.isPrimitive())
			return false;

		if (cls.equals(String.class) || cls.equals(LocalDateTime.class) || cls.equals(DTObject.class)
				|| cls.equals(Instant.class) || cls.equals(ZonedDateTime.class))
			return true;

		return false;

	}

	public static boolean getValueCodeIsString(Object value) {
		var valueClass = value.getClass();
		if (valueClass.equals(String.class) || valueClass.equals(char.class))
			return true;
		if (valueClass.equals(DTObject.class))
			return false;
		boolean isPrimitive = valueClass.isPrimitive();
		if (isPrimitive)
			return false;
		return true;
	}

}
