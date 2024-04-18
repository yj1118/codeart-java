package apros.codeart.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReaderWriterLockSlim {
	private final ReentrantReadWriteLock _lock;

	public ReaderWriterLockSlim() {
		_lock = new ReentrantReadWriteLock();
	}

	/**
	 * 以读模式执行操作
	 * 
	 * @param action
	 */
	public void readRun(Runnable action) {
		_lock.readLock().lock();
		try {
			action.run();
		} finally {
			_lock.readLock().unlock();
		}
	}

	/**
	 * 以读模式得到数据
	 * 
	 * @param <T>
	 * @param action
	 * @return
	 */
	public <T> T readGet(Supplier<T> action) {
		_lock.readLock().lock();
		try {
			return action.get();
		} finally {
			_lock.readLock().unlock();
		}
	}

	public void writeRun(Runnable action) {
		_lock.writeLock().lock();
		try {
			action.run();
		} finally {
			_lock.writeLock().unlock();
		}
	}

	public <T> void writeRun(Consumer<T> action, T arg) {
		_lock.writeLock().lock();
		try {
			action.accept(arg);
		} finally {
			_lock.writeLock().unlock();
		}
	}

}
