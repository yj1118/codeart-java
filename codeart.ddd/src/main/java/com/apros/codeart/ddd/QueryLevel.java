package com.apros.codeart.ddd;

import com.apros.codeart.runtime.TypeUtil;

public final class QueryLevel {

	private int _code;

	public int code() {
		return _code;
	}

	private QueryLevel(int code) {
		_code = code;
	}

	public static final int NoneCode = 1;
	public static final int ShareCode = 2;
	public static final int SingleCode = 3;
	public static final int HoldSingleCode = 4;
	public static final int MirroringCode = 5;

	/**
	 * 无锁
	 */
	public static final QueryLevel None = new QueryLevel(NoneCode);

	/**
	 * 只有一个会话可以访问查询的结果集，其余线程将等待
	 */
	public static final QueryLevel Single = new QueryLevel(SingleCode);

	/**
	 * 只有一个线程可以访问查询的结果集或者满足查询条件的不存在的数据，其余线程将等待
	 * 
	 * 也就是说，HoldSingle锁可以保证满足查询条件的现有数据和即将插入的数据都被锁住
	 * 
	 * 开启互斥锁和HoldSingle 可以防止在查询中别的线程插入数据
	 */
	public static final QueryLevel HoldSingle = new QueryLevel(HoldSingleCode);

	/**
	 * 共享锁，当前线程开启此线程后，不会影响其他线程获取Share、None
	 * 但是其他线程不能立即获取Single或HoldSingle锁，需要等待只读线程操作完成后才行；
	 * 另外，当其他线程正在Single或者HoldSingle,当前线程也无法获得只读锁，需要等待
	 */
	public static final QueryLevel Share = new QueryLevel(ShareCode);

	/**
	 * 以镜像的形式加载对象，该模式下不会从缓冲区中获取对象而是直接以无锁的模式加载全新的对象
	 */
	public static final QueryLevel Mirroring = new QueryLevel(MirroringCode);

	@Override
	public boolean equals(Object obj) {

		QueryLevel target = TypeUtil.as(obj, QueryLevel.class);
		if (target == null)
			return false;
		return this.code() == target.code();
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(this.code());
	}
}
