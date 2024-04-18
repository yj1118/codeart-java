package apros.codeart.ddd.repository;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.util.EventHandler;

public abstract class PersistRepository implements IPersistRepository {

	/// <summary>
	/// 将对象添加到持久层中
	/// </summary>
	/// <param name="obj"></param>
	public abstract void persistAdd(IAggregateRoot obj);

	/// <summary>
	/// 修改对象在持久层中的信息
	/// </summary>
	/// <param name="obj"></param>
	public abstract void persistUpdate(IAggregateRoot obj);

	/// <summary>
	/// 从持久层中删除对象
	/// </summary>
	/// <param name="obj"></param>
	public abstract void persistDelete(IAggregateRoot obj);

//	#region 事件

	private EventHandler<RepositoryPrePersistEventArgs> _prePersist;

	public EventHandler<RepositoryPrePersistEventArgs> prePersist() {
		if (_prePersist == null)
			_prePersist = new EventHandler<RepositoryPrePersistEventArgs>();
		return _prePersist;
	}

	/// <summary>
	/// 执行仓储操作之前
	/// </summary>
	/// <param name="obj"></param>
	/// <param name="action"></param>
	/// <returns></returns>
	protected boolean onPrePersist(IAggregateRoot obj, RepositoryAction action) {
		if (_prePersist != null) {
			var args = new RepositoryPrePersistEventArgs(obj, action);
			_prePersist.raise(this, () -> args);
			return args.allow();
		}
		return true;
	}

	private EventHandler<RepositoryPersistedEventArgs> _persisted;

	public EventHandler<RepositoryPersistedEventArgs> persisted() {
		if (_persisted == null)
			_persisted = new EventHandler<RepositoryPersistedEventArgs>();
		return _persisted;
	}

	/// <summary>
	/// 执行仓储操作之后
	/// </summary>
	/// <param name="obj"></param>
	/// <param name="action"></param>
	protected void onPersisted(IAggregateRoot obj, RepositoryAction action) {
		if (_persisted != null) {
			_persisted.raise(this, () -> new RepositoryPersistedEventArgs(obj, action));
		}
	}

	private EventHandler<RepositoryRollbackEventArgs> _rollback;

	public EventHandler<RepositoryRollbackEventArgs> rollback() {
		if (_rollback == null)
			_rollback = new EventHandler<RepositoryRollbackEventArgs>();
		return _rollback;
	}

	public void onRollback(Object sender, RepositoryRollbackEventArgs e) {
		if (_rollback != null) {
			_rollback.raise(sender, () -> e);
		}
	}

	public void onAddCommited(IAggregateRoot obj) {

	}

	public void onUpdateCommited(IAggregateRoot obj) {

	}

	public void onDeleteCommited(IAggregateRoot obj) {

	}

}
