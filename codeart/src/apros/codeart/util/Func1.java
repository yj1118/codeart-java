package apros.codeart.util;

@FunctionalInterface
public interface Func1<T, R> {
	R apply(T t) throws Exception;
}