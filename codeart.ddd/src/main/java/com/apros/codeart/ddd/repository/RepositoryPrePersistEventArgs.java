package com.apros.codeart.ddd.repository;

import com.apros.codeart.ddd.IAggregateRoot;

public class RepositoryPrePersistEventArgs {

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

	private boolean _allow;

	/**
	 * 获取或设置是否允许继续执行后续操作
	 * 
	 * @return
	 */
	public boolean allow() {
		return _allow;
	}

	public void allow(boolean value) {
		_allow = value;
	}

	public RepositoryPrePersistEventArgs(IAggregateRoot target, RepositoryAction action) {
		_target = target;
		_action = action;
	}

}
