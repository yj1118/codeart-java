package apros.codeart.util;

/**
 * @param <T>
 * @param <U>
 * @param <V>
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
	void accept(T t, U u, V v);
}