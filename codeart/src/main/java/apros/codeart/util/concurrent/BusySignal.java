package apros.codeart.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BusySignal<T> {

	private final AtomicBoolean _ready = new AtomicBoolean(false);

	private volatile T _value;

	public BusySignal() {
	}

	public T wait(long timeout, TimeUnit unit) {
		long startTime = System.nanoTime();
		long timeoutNanos = unit.toNanos(timeout);

		// 以下代码保证了最高即时性，切没有线程调度的开销，但是会占用一定的CPU资源
		while (!_ready.getAcquire()) {
			// 检查是否超时
			if (System.nanoTime() - startTime > timeoutNanos) {
				throw new SignalTimeoutException();
			}
		}
		return _value;
	}

	/**
	 * 设置一个信号量
	 * 
	 * @param value
	 */
	public void set(T value) {
		_value = value;
		_ready.setRelease(true);
	}
}
