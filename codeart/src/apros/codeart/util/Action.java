package apros.codeart.util;

@FunctionalInterface
public interface Action<T> {
	void apply(T t) throws Exception;
}
