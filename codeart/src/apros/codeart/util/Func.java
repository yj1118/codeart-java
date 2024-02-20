package apros.codeart.util;

@FunctionalInterface
public interface Func<R> {
	R apply() throws Exception;
}