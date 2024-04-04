package com.apros.codeart.ddd.repository;

import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.i18n.Language;
import com.google.common.collect.Iterables;

public final class LockManager {
	public static void lock(Iterable<IAggregateRoot> roots) {
		if (roots == null || Iterables.size(roots) == 0)
			return;

		if (_manager == null)
			throw new DomainDrivenException(Language.strings("NotExistLockManager"));
		_manager.lock(roots);
	}

	private static ILockManager _manager;

	public static void register(ILockManager manager) {
		_manager = manager;
	}
}
