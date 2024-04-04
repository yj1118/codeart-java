package com.apros.codeart.ddd.repository;

import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.StatusEvent;

public abstract class AbstractRepository<TRoot extends IAggregateRoot> extends PersistRepository
		implements IRepository {

	// #region 增加数据

	protected void registerAdded(IAggregateRoot obj) {
		DataContext.Current.RegisterAdded(obj, this);
	}

	protected void registerRollbackAdd(IAggregateRoot obj) {
		var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Add);
		DataContext.Current.RegisterRollback(args);
	}

	@SuppressWarnings("unchecked")
	public void addRoot(IAggregateRoot obj) {
		add((TRoot) obj);
	}

	public void add(TRoot obj) {
		if (obj.isEmpty())
			return;

		DataContext.Using(() -> {
			RegisterRollbackAdd(obj);
			StatusEvent.Execute(StatusEventType.PreAdd, obj);
			obj.OnPreAdd();
			RegisterAdded(obj);
			obj.OnAdded();
			StatusEvent.Execute(StatusEventType.Added, obj);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void persistAdd(IAggregateRoot obj) {
		if (obj.isEmpty())
			return;
		TRoot root = (TRoot) obj;
		if (this.onPrePersist(obj, RepositoryAction.Add)) {
			persistAddRoot(root);
		}
		this.onPersisted(obj, RepositoryAction.Add);
	}

	protected abstract void persistAddRoot(TRoot obj);

//	region 修改数据

	protected void registerRollbackUpdate(IAggregateRoot obj) {
		var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Update);
		DataContext.Current.registerRollback(args);
	}

	protected void registerUpdated(IAggregateRoot obj) {
		DataContext.Current.registerUpdated(obj, this);
	}

	@SuppressWarnings("unchecked")
	public void updateRoot(IAggregateRoot obj) {
		update((TRoot) obj);
	}

	public void update(TRoot obj) {
		if (obj.isEmpty())
			return;

		DataContext.Using(() -> {
			RegisterRollbackUpdate(obj);
			StatusEvent.Execute(StatusEventType.PreUpdate, obj);
			obj.OnPreUpdate();
			RegisterUpdated(obj);
			obj.OnUpdated();
			StatusEvent.Execute(StatusEventType.Updated, obj);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void persistUpdate(IAggregateRoot obj) {
		if (obj.isEmpty())
			return;
		TRoot root = (TRoot) obj;
		if (this.onPrePersist(obj, RepositoryAction.Update)) {
			persistUpdateRoot(root);
		}
		this.onPersisted(obj, RepositoryAction.Update);
	}

	protected abstract void persistUpdateRoot(TRoot obj);

//	region 删除数据

	protected void registerRollbackDelete(IAggregateRoot obj) {
		var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Delete);
		DataContext.Current.RegisterRollback(args);
	}

	protected void registerDeleted(IAggregateRoot obj) {
		DataContext.Current.RegisterDeleted(obj, this);
	}

	@SuppressWarnings("unchecked")
	public void deleteRoot(IAggregateRoot obj) {
		update((TRoot) obj);
	}

	public void delete(TRoot obj) {
		if (obj.isEmpty())
			return;

		DataContext.Using(() -> {
			RegisterRollbackDelete(obj);
			StatusEvent.Execute(StatusEventType.PreDelete, obj);
			obj.OnPreDelete();
			RegisterDeleted(obj);
			obj.OnDeleted();
			StatusEvent.Execute(StatusEventType.Deleted, obj);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void persistDelete(IAggregateRoot obj) {
		if (obj.isEmpty())
			return;
		TRoot root = (TRoot) obj;
		if (root != null) {
			if (this.onPrePersist(obj, RepositoryAction.Delete)) {
				persistDeleteRoot(root);
			}
			this.onPersisted(obj, RepositoryAction.Delete);
		}
	}

	protected abstract void persistDeleteRoot(TRoot obj);

//	region 查询数据

	@Override
public	IAggregateRoot findRoot(Object id, QueryLevel level)
{
    TRoot result = null;
    DataContext.Using(()->
    {
        result = DataContext.Current.registerQueried<TRoot>(level, () ->
        {
            return persistFind(id, level);
        });
    });
    return result;
}

	protected abstract TRoot persistFind(Object id, QueryLevel level);

}
