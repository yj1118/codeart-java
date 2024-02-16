package apros.codeart.pooling.util;

import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.pooling.IPoolItem;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.PoolItemPhase;
import apros.codeart.pooling.PoolingException;
import apros.codeart.util.StringUtil;

public final class StringPool {
	private StringPool() {
	}

	private static Pool<StringBuilder> _pool = new Pool<StringBuilder>(() -> {
		return new StringBuilder();
	}, (str, phase) -> {
		if (phase == PoolItemPhase.Returning) {
			StringUtil.clear(str);
		}
		return true;
	}, PoolConfig.onlyMaxRemainTime(300));

	public static IPoolItem<StringBuilder> borrow() throws PoolingException {
		return _pool.borrow();
	}

	public static String using(Consumer<StringBuilder> action) throws PoolingException {
		String result = null;
		try (var item = borrow()) {
			var str = item.getItem();
			action.accept(str);
			result = str.toString();
		}
		return result;
	}

	public static String join(String separator, Iterable<String> items) throws PoolingException {
		return StringPool.using((msg) -> {
			var i = 0;
			for (var item : items) {
				if (i > 0)
					msg.append(separator);
				msg.append(item);
				i++;
			}
		});
	}

	public static <T> String join(String separator, Iterable<T> items, Function<T, String> map)
			throws PoolingException {
		return StringPool.using((msg) -> {
			var i = 0;
			for (var item : items) {
				if (i > 0)
					msg.append(separator);
				msg.append(map.apply(item));
				i++;
			}
		});
	}

	/**
	 * 转换成多行
	 * 
	 * @param items
	 * @return
	 * @throws PoolingException
	 */
	public static String lines(Iterable<String> items) throws PoolingException {
		return join(System.lineSeparator(), items);
	}

	public static <T> String lines(Iterable<T> items, Function<T, String> map) throws PoolingException {
		return join(System.lineSeparator(), items, map);
	}

}