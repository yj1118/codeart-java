package apros.codeart.pooling;

public interface IPool extends AutoCloseable {
	void clear(IPoolItem item);
}
