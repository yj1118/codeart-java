package apros.codeart.pooling;

public class PoolConfig {

	public PoolConfig() {
		_fetchOrder = PoolFetchOrder.Lifo;
		_loanCapacity = 0; // 不限制
		_poolCapacity = 0; // 不限制
		_maxUses = 0; // 不限制
		_maxLifespan = 0; // 不限制
		_maxRemainTime = 0;
	}

	public PoolConfig(int loanCapacity) {
		this();
		_loanCapacity = loanCapacity;
	}

	public static PoolConfig onlyMaxRemainTime(int maxRemainTime) {
		var config = new PoolConfig();
		config.setMaxRemainTime(maxRemainTime);
		return config;
	}

	private PoolFetchOrder _fetchOrder;

	/**
	 * 
	 * 获取调用池的方式。
	 * 
	 * 该值可以是PoolFetchOrder.Fifo或PoolFetchOrder.Lifo
	 * 
	 * PoolFetchOrder.Lifo项会导致大量的内存用于管理池，这是因为较低的代比交高的代更可能被抛弃
	 * 
	 * @param fetchOrder
	 */
	public PoolFetchOrder getFetchOrder() {
		return _fetchOrder;
	}

	/**
	 * 
	 * 设置调用池的方式。
	 * 
	 * 该值可以是PoolFetchOrder.Fifo或PoolFetchOrder.Lifo
	 * 
	 * PoolFetchOrder.Lifo项会导致大量的内存用于管理池，这是因为较低的代比交高的代更可能被抛弃
	 * 
	 * @param fetchOrder
	 */
	public void setFetchOrder(PoolFetchOrder fetchOrder) {
		this._fetchOrder = fetchOrder;
	}

	private int _loanCapacity;

	/**
	 * 获取最大能够借出的项的数量。
	 * 
	 * 如果线程池中借出的项数量达到该值，那么下次在借用项时，调用线程将被阻塞，直到有项被返回到线程池中。
	 * 
	 * 如果该值小于或者等于0，那么项会被马上借给调用线程，默认值是0（无限）
	 */
	public int getLoanCapacity() {
		return _loanCapacity;
	}

	/**
	 * 设置最大能够借出的项的数量。
	 * 
	 * 如果线程池中借出的项数量达到该值，那么下次在借用项时，调用线程将被阻塞，直到有项被返回到线程池中。
	 * 
	 * 如果该值小于或者等于0，那么项会被马上借给调用线程，默认值是0（无限）
	 * 
	 * @param loanCapacity
	 */
	public void setLoanCapacity(int loanCapacity) {
		_loanCapacity = loanCapacity;
	}

	private int _poolCapacity;

	/**
	 * 获取池中可容纳的最大项数量。
	 * 
	 * 当项被返回到池中时，如果池的容量已达到最大值，那么该项将被抛弃。
	 * 
	 * 如果该值小于或等于0，代表无限制
	 * 
	 * 借出数的限制LoanCapacity 会引起阻塞，但PoolCapacity限制是抛弃项，而不是阻塞，两者使用的场景不同。
	 * 
	 * @return
	 */
	public int getPoolCapacity() {
		return _poolCapacity;
	}

	/**
	 * 设置池中可容纳的最大项数量。
	 * 
	 * 当项被返回到池中时，如果池的容量已达到最大值，那么该项将被抛弃。
	 * 
	 * 如果该值小于或等于0，代表无限制
	 * 
	 * 借出数的限制LoanCapacity 会引起阻塞，但PoolCapacity限制是抛弃项，而不是阻塞，两者使用的场景不同。
	 * 
	 */
	public void setPoolCapacity(int poolCapacity) {
		_poolCapacity = poolCapacity;
	}

	/**
	 * 
	 * 获取或设置池中每一项的最大寿命（单位秒） 如果该值小于或者等于0，则代表允许池中的项无限制存在
	 * 
	 */
	private int _maxLifespan;

	/**
	 * 
	 * 获取池中每一项的最大寿命（单位秒） 如果该值小于或者等于0，则代表允许池中的项无限制存在
	 * 
	 */
	public int getMaxLifespan() {
		return _maxLifespan;
	}

	/**
	 * 
	 * 设置池中每一项的最大寿命（单位秒） 如果该值小于或者等于0，则代表允许池中的项无限制存在
	 * 
	 */
	public void setMaxLifespan(int maxLifespan) {
		_maxLifespan = maxLifespan;
	}

	/**
	 * 
	 * 获取或设置池中每一项的停留时间（单位秒）。
	 * 
	 * 如果项在池中超过停留时间，那么抛弃。
	 * 
	 * 如果项被使用，那么停留时间会被重置计算。
	 * 
	 * 如果该值小于或者等于0，则代表允许池中的项无限制存在
	 */
	private int _maxRemainTime;

	public int getMaxRemainTime() {
		return _maxRemainTime;
	}

	public void setMaxRemainTime(int maxRemainTime) {
		_maxRemainTime = maxRemainTime;
	}

	/**
	 * /获取或设置池中项在被移除或销毁之前，能使用的最大次数。 如果该值小于或等于0，那么可以使用无限次
	 */
	private int _maxUses;

	public int getMaxUses() {
		return _maxUses;
	}

	public void setMaxUses(int maxUses) {
		_maxUses = maxUses;
	}

}
