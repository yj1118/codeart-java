package apros.codeart.pooling;

import java.util.LinkedList;

/**
 * 先进先出的容器
 * 
 * @param <R>
 */
class FifoContainer<R> implements IPoolContainer<R> {
	private final LinkedList<R> _queue;

	public FifoContainer() {
		_queue = new LinkedList<R>();
	}

	@Override
	public R take() {
		// 取出时，取的是顶部对象（顶部对象是在容器中呆的比较久的对象）
		return _queue.removeFirst();
	}

	@Override
	public void put(R item) {
		// 新来的对象，放在结尾处，这样顶部对象就是呆的比较久的对象
		_queue.addLast(item);
	}

	@Override
	public int getCount() {
		return _queue.size();
	}
}