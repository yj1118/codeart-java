package com.apros.codeart.ddd;

import java.time.LocalDateTime;

public class EmptyableDateTime extends Emptyable<LocalDateTime> {

	public EmptyableDateTime(LocalDateTime value) {
		super(value);
	}

	public static EmptyableDateTime createEmpty() {
		return new EmptyableDateTime(null);
	}

	public final static Class<?> ValueType = LocalDateTime.class;

}
