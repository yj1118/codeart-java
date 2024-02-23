package com.apros.codeart.pooling;

import static com.apros.codeart.runtime.Util.as;

public final class Util {
	private Util() {
	}

	/**
	 * 停止一个对象的使用，对于可重复利用的对象，需要清理(clear),对于可关闭的对象，需要关闭
	 * 
	 * @param item
	 * @throws Exception
	 */
	public static void stop(Object item) throws Exception {
		var reusable = as(item, IReusable.class);
		if (reusable != null) {
			reusable.clear();
			return;
		}

		var closeable = as(item, AutoCloseable.class);
		if (closeable != null) {
			closeable.close();
			return;
		}
	}

	public static void stop(Iterable<Object> items) throws Exception {
		for (var item : items) {
			stop(item);
		}
	}

}
