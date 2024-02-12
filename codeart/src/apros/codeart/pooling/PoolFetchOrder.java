package apros.codeart.pooling;

public enum PoolFetchOrder {

	/**
	 * 就像队列那样,使用先入先出的算法
	 */
	Fifo,

	/**
	 * 就像堆栈那样，使用后入先出的算法。
	 * 
	 * 该配置会导致大量的内存用于管理池，这是因为较低的代比交高的代更可能被抛弃(.net机制，java下待研究)
	 */
	Lifo
}
