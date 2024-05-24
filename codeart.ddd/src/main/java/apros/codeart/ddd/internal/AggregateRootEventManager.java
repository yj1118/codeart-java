package apros.codeart.ddd.internal;

import java.util.ArrayList;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.StatusEventType;
import apros.codeart.ddd.repository.RepositoryEventArgs;
import apros.codeart.ddd.repository.RepositoryRollbackEventArgs;
import apros.codeart.i18n.Language;
import apros.codeart.util.EventHandler;

public final class AggregateRootEventManager {

	private IAggregateRoot _root;

	public AggregateRootEventManager(IAggregateRoot root) {
		_root = root;
	}

	private EventHandler<RepositoryRollbackEventArgs> _rollback;

	/**
	 * 仓储操作回滚事件
	 */
	public EventHandler<RepositoryRollbackEventArgs> rollback() {
		if (_rollback == null)
			_rollback = new EventHandler<RepositoryRollbackEventArgs>();
		return _rollback;
	}

	public void onRollback(Object sender, RepositoryRollbackEventArgs e) {
		if (this._rollback != null) {
			this._rollback.raise(sender, () -> e);
		}
	}

	private EventHandler<RepositoryEventArgs> _preAdd;

	public EventHandler<RepositoryEventArgs> preAdd() {
		if (_preAdd == null)
			_preAdd = new EventHandler<RepositoryEventArgs>();
		return _preAdd;
	}

	public void onPreAdd() {
		if (_root.invalid())
			throw new DomainDrivenException(Language.strings("apros.codeart.ddd", "ObjectInvalid", "add"));

		if (_preAdd != null) {
			_preAdd.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.PreAdd);
			});
		}
	}

	private EventHandler<RepositoryEventArgs> _added;

	public EventHandler<RepositoryEventArgs> added() {
		if (_added == null)
			_added = new EventHandler<RepositoryEventArgs>();
		return _added;
	}

	public void onAdded() {
		if (_added != null) {
			_added.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.Added);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _addPreCommit;

	public EventHandler<RepositoryEventArgs> addPreCommit() {
		if (_addPreCommit == null)
			_addPreCommit = new EventHandler<RepositoryEventArgs>();
		return _addPreCommit;
	}

	public void onAddPreCommit() {
		if (_addPreCommit != null) {
			_addPreCommit.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.AddPreCommit);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _addCommitted;

	public EventHandler<RepositoryEventArgs> addCommitted() {
		if (_addCommitted == null)
			_addCommitted = new EventHandler<RepositoryEventArgs>();
		return _addCommitted;
	}

	public void onAddCommitted() {
		if (_addCommitted != null) {
			_addCommitted.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.AddCommitted);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _preUpdate;

	public EventHandler<RepositoryEventArgs> preUpdate() {
		if (_preUpdate == null)
			_preUpdate = new EventHandler<RepositoryEventArgs>();
		return _preUpdate;
	}

	public void onPreUpdate() {
		if (_root.invalid())
			throw new DomainDrivenException(Language.strings("apros.codeart.ddd", "ObjectInvalid", "update"));

		if (_preUpdate != null) {
			_preUpdate.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.PreUpdate);
			});
		}
	}

	private EventHandler<RepositoryEventArgs> _updated;

	public EventHandler<RepositoryEventArgs> updated() {
		if (_updated == null)
			_updated = new EventHandler<RepositoryEventArgs>();
		return _updated;
	}

	public void onUpdated() {
		if (_updated != null) {
			_updated.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.Updated);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _updatePreCommit;

	public EventHandler<RepositoryEventArgs> updatePreCommit() {
		if (_updatePreCommit == null)
			_updatePreCommit = new EventHandler<RepositoryEventArgs>();
		return _updatePreCommit;
	}

	public void onUpdatePreCommit() {
		if (_updatePreCommit != null) {
			_updatePreCommit.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.UpdatePreCommit);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _updateCommitted;

	public EventHandler<RepositoryEventArgs> updateCommitted() {
		if (_updateCommitted == null)
			_updateCommitted = new EventHandler<RepositoryEventArgs>();
		return _updateCommitted;
	}

	public void onUpdateCommitted() {
		if (_updateCommitted != null) {
			_updateCommitted.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.UpdateCommitted);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _preDelete;

	public EventHandler<RepositoryEventArgs> preDelete() {
		if (_preDelete == null)
			_preDelete = new EventHandler<RepositoryEventArgs>();
		return _preDelete;
	}

	public void onPreDelete() {
		if (_preDelete != null) {
			_preDelete.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.PreDelete);
			});
		}
	}

	private EventHandler<RepositoryEventArgs> _deleted;

	public EventHandler<RepositoryEventArgs> deleted() {
		if (_deleted == null)
			_deleted = new EventHandler<RepositoryEventArgs>();
		return _deleted;
	}

	public void onDeleted() {
		if (_deleted != null) {
			_deleted.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.Deleted);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _deletePreCommit;

	public EventHandler<RepositoryEventArgs> deletePreCommit() {
		if (_deletePreCommit == null)
			_deletePreCommit = new EventHandler<RepositoryEventArgs>();
		return _deletePreCommit;
	}

	public void onDeletePreCommit() {
		if (_deletePreCommit != null) {
			_deletePreCommit.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.DeletePreCommit);
			});
		}
		callOnceRepositoryActions();
	}

	private EventHandler<RepositoryEventArgs> _deleteCommitted;

	public EventHandler<RepositoryEventArgs> deleteCommitted() {
		if (_deleteCommitted == null)
			_deleteCommitted = new EventHandler<RepositoryEventArgs>();
		return _deleteCommitted;
	}

	public void onDeleteCommitted() {
		if (_deleteCommitted != null) {
			_deleteCommitted.raise(this, () -> {
				return new RepositoryEventArgs(_root, StatusEventType.DeleteCommitted);
			});
		}
		callOnceRepositoryActions();
	}

	private ArrayList<Runnable> _onceRepositoryCallbackActions = null;

	/**
	 * 在下次执行完该对象的仓储操作后执行 {@code action} 动作该动作仅被执行一次
	 * 
	 * @param action
	 */
	public void onceRepositoryCallback(Runnable action) {
		if (_onceRepositoryCallbackActions == null)
			_onceRepositoryCallbackActions = new ArrayList<Runnable>();
		_onceRepositoryCallbackActions.add(action);
	}

	private void callOnceRepositoryActions() {
		if (_onceRepositoryCallbackActions == null)
			return;
		for (var action : _onceRepositoryCallbackActions)
			action.run();
		_onceRepositoryCallbackActions.clear(); // 执行完后清空行为集合
	}

}
