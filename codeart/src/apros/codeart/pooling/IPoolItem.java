package apros.codeart.pooling;

public interface IPoolItem<T> extends AutoCloseable {

	/**
	 * 获取项所属的池
	 * 
	 * @return
	 */
	Pool<T> getOwner();

	/**
	 * 获取项
	 * 
	 * @return
	 */
	T getItem();

	/**
	 * 获取或设置一个值，表示该项是否是损坏的，如果是损坏的，那么应该从它所属的Pool{T}实例中将该项移除
	 * 
	 * @return
	 */
	boolean isCorrupted();

}
