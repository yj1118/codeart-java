package com.apros.codeart.ddd.repository;

import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.util.EventHandler;

public interface IPersistRepository {
	/// <summary>
	/// 将对象添加到持久层中
	/// </summary>
	/// <param name="obj"></param>
	void persistAdd(IAggregateRoot obj);

	/// <summary>
	/// 修改对象在持久层中的信息
	/// </summary>
	/// <param name="obj"></param>
	void persistUpdate(IAggregateRoot obj);

	/// <summary>
	/// 从持久层中删除对象
	/// </summary>
	/// <param name="obj"></param>
	void persistDelete(IAggregateRoot obj);

	EventHandler<RepositoryPersistedEventArgs> persisted();

	EventHandler<RepositoryPrePersistEventArgs> prePersist();

	EventHandler<RepositoryRollbackEventArgs> rollback();

	void onRollback(Object sender, RepositoryRollbackEventArgs e);

	void onAddCommited(IAggregateRoot obj);

	void onUpdateCommited(IAggregateRoot obj);

	void onDeleteCommited(IAggregateRoot obj);
}
