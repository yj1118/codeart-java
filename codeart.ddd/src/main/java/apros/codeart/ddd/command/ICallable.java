package apros.codeart.ddd.command;

/**
 * 
 * 带返回值的命令
 * 
 * @param <T>
 */
public interface ICallable<T> {
	T execute();
}
