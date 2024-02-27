package com.apros.codeart.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReaderWriterLockSlim {
	private final ReentrantReadWriteLock _typeMethodsLock = new ReentrantReadWriteLock();

	public ReaderWriterLockSlim() {

	}

	public void read(Runnable action) {
		_typeMethodsLock.readLock().lock();
		try {
			action.run();
		} finally {
			_typeMethodsLock.readLock().unlock();
		}

	}

}
