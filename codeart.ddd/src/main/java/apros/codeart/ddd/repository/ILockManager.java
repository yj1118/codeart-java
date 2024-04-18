package apros.codeart.ddd.repository;

import apros.codeart.ddd.IAggregateRoot;

public interface ILockManager {

	/**
	 * 以不会造成死锁的形式锁定指定的聚合根对象
	 * 
	 * @param roots
	 */
	void lock(Iterable<IAggregateRoot> roots);
}
