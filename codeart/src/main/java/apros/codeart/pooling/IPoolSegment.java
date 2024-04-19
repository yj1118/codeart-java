package apros.codeart.pooling;

public interface IPoolSegment extends AutoCloseable {
	void clear(IPoolItem item);
}
