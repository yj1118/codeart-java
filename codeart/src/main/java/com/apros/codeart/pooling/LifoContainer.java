package com.apros.codeart.pooling;

import java.util.LinkedList;

/**
 * 后进先出的容器
 * 
 * @param <R>
 */
class LifoContainer<R> implements IPoolContainer<R> {
	private final LinkedList<R> _linked;

	public LifoContainer() {
		_linked = new LinkedList<R>();
	}

	@Override
	public R take() {
		// 取出时，取的是顶部对象（顶部对象是新进来的对象）
		return _linked.removeFirst();
	}

	@Override
	public void put(R item) {
		// 新来的对象，放在顶部，这样顶部对象就是最新进来的对象，底部的对象是呆的比较久的对象
		_linked.addFirst(item);
	}

	@Override
	public int getCount() {
		return _linked.size();
	}
}