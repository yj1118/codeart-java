package com.apros.codeart.dto;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;

final class Util {
	private Util() {
	}

	public static <T> AbstractList<T> createList(boolean isReadOnly, int count) {
		return isReadOnly ? new ArrayList<T>(count) : new LinkedList<T>();
	}

}
