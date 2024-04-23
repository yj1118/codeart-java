package apros.codeart.pooling.util;

import java.util.function.Consumer;

import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;

public final class StringPool {
	private StringPool() {
	}

	private static Pool<StringBuilder> _pool = new Pool<StringBuilder>(StringBuilder.class, new PoolConfig(10, 200),
			(isTempItem) -> {
				return new StringBuilder(100);
			});

	public static String using(Consumer<StringBuilder> action) {
		try (var temp = _pool.borrow()) {
			StringBuilder sb = temp.getItem();
			action.accept(sb);
			return sb.toString();
		}
	}
}
