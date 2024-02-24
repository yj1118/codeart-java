package com.apros.codeart.pooling;

import static com.apros.codeart.runtime.Util.as;
import static com.apros.codeart.runtime.Util.propagate;

public final class Util {
	private Util() {
	}

	/**
	 * 停止一个对象的使用，对于可重复利用的对象，需要清理(clear),对于可关闭的对象，需要关闭
	 * 
	 * @param item
	 * @throws Exception
	 */
	public static void stop(Object item) {
		var reusable = as(item, IReusable.class);
		if (reusable != null) {
			reusable.clear();
			return;
		}

		try {
			var closeable = as(item, AutoCloseable.class);
			if (closeable != null) {
				closeable.close();
				return;
			}
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	public static void stop(Iterable<Object> items) {
		for (var item : items) {
			stop(item);
		}
	}

}
