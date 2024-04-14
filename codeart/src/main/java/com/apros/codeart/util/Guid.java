package com.apros.codeart.util;

import java.util.UUID;

public class Guid {

	public static final UUID Empty = UUID.fromString("00000000-0000-0000-0000-000000000000");

	/**
	 * 获得不带分隔符的guid
	 * 
	 * @return
	 */
	public static String compact() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}

	public static boolean isNullOrEmpty(UUID value) {
		return value == null || value.equals(Empty);
	}

	public static UUID NewGuid() {
		return UUID.randomUUID();
	}

}
