package apros.codeart.ddd.repository;

import apros.codeart.ddd.IAggregateRoot;

public class RepositoryPersistedEventArgs {

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

	public RepositoryPersistedEventArgs(IAggregateRoot target, RepositoryAction action) {
		_target = target;
		_action = action;
	}
}
