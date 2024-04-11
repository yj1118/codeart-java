package com.apros.codeart.runtime;

public class Util {

	public static RuntimeException propagate(Throwable throwable) {
		return new RuntimeException(throwable);
	}

}