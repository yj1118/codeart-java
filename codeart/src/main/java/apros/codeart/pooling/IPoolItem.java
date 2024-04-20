package apros.codeart.pooling;

public interface IPoolItem extends AutoCloseable {

	/**
	 * 获取项
	 * 
	 * @return
	 */
	<T> T getItem();

	/**
	 * 归还项
	 */
	void back();

	void close();
}
