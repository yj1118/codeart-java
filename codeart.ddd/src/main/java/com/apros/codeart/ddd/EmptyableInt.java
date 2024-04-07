package com.apros.codeart.ddd;

public class EmptyableInt extends Emptyable<Integer> {

	public EmptyableInt(Integer value) {
		super(value);
	}

	public static EmptyableInt createEmpty() {
		return new EmptyableInt(null);
	}

	public final static Class<?> ValueType = int.class;

	public final static EmptyableInt Empty = new EmptyableInt(null);

}
