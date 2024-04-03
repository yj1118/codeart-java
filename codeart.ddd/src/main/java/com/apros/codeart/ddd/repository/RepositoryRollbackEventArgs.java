package com.apros.codeart.ddd.repository;

import com.apros.codeart.ddd.IAggregateRoot;

public class RepositoryRollbackEventArgs {
	private IAggregateRoot _target;

	public IAggregateRoot target() {
		return _target;
	}

	private RepositoryAction _action;

	/**
	 * 仓储行为
	 * 
	 * @return
	 */
	public RepositoryAction action() {
		return _action;
	}

	private IPersistRepository _repository;

	/**
	 * 相关仓储
	 * 
	 * @return
	 */
	public IPersistRepository repository() {
		return _repository;
	}

	public RepositoryRollbackEventArgs(IAggregateRoot target, IPersistRepository repository, RepositoryAction action) {
		_target = target;
		_repository = repository;
		_action = action;
	}
}
