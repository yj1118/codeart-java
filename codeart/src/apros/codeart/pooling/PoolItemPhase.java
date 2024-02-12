package apros.codeart.pooling;

enum PoolItemPhase {

	/**
	 * 表示IPoolItem{T}已离开Pool{T},离开是指移除，不是借出去
	 */
	Leaving,

	/**
	 * 表示IPoolItem{T}已回到Pool{T}
	 */
	Returning
}
