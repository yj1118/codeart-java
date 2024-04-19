package apros.codeart.pooling;

public interface IPoolItem extends AutoCloseable {

	/**
	 * 获取项所属的池
	 * 
	 * @return
	 */
	IPoolSegment getOwner();

	/**
	 * 获取项
	 * 
	 * @return
	 */
	<T> T getItem();

	void close();
}
