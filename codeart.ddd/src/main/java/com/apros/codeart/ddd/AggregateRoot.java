package com.apros.codeart.ddd;

@MergeDomain
@FrameworkDomain
public abstract class AggregateRoot extends EntityObject implements IAggregateRoot {
	public AggregateRoot() {
		initRemotable();
		this.onConstructed();
	}

	#region 实现内聚根接口

	private string _uniqueKey;

public string UniqueKey
{
    get
    {
        if (_uniqueKey == null)
        {
            _uniqueKey = UniqueKeyCalculator.GetUniqueKey(this);
        }
        return _uniqueKey;
    }
}

/// <summary>
/// 仓储操作回滚事件
/// </summary>
	public event RepositoryRollbackEventHandler Rollback;

public virtual void OnRollback(object sender, RepositoryRollbackEventArgs e)
{
    if (this.Rollback != null)
    {
        this.Rollback(sender, e);
    }
}

	public event RepositoryEventHandler PreAdd;

public virtual void OnPreAdd()
{
    if (this.Invalid) throw new DomainDrivenException("对象无效，不能执行新增操作");

    if (this.PreAdd != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.PreAdd);
        this.PreAdd(this, e);
    }

}

	public event RepositoryEventHandler Added;

public virtual void OnAdded()
{
    if (this.Added != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.Added);
        this.Added(this, e);
    }
    CallOnceRepositoryActions();
}

	public event RepositoryEventHandler AddPreCommit;

public virtual void OnAddPreCommit()
{
    if (this.AddPreCommit != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.AddPreCommit);
        this.AddPreCommit(this, e);
    }
    CallOnceRepositoryActions();
}

	public event RepositoryEventHandler AddCommitted;

public virtual void OnAddCommitted()
{
    if (this.AddCommitted != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.AddCommitted);
        this.AddCommitted(this, e);
    }
    CallOnceRepositoryActions();
}

	public event RepositoryEventHandler PreUpdate;

public virtual void OnPreUpdate()
{
    if (this.Invalid) throw new DomainDrivenException("对象无效，不能执行修改操作");

    if (this.PreUpdate != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.PreUpdate);
        this.PreUpdate(this, e);
    }
}

	public event RepositoryEventHandler Updated;

public virtual void OnUpdated()
{
    if (this.Updated != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.Updated);
        this.Updated(this, e);
    }
    CallOnceRepositoryActions();
}

	public event RepositoryEventHandler UpdatePreCommit;

public virtual void OnUpdatePreCommit()
{
    if (this.UpdatePreCommit != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.UpdatePreCommit);
        this.UpdatePreCommit(this, e);
    }
    CallOnceRepositoryActions();
}

	public event RepositoryEventHandler UpdateCommitted;

public virtual void OnUpdateCommitted()
{
    if (this.UpdateCommitted != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.UpdateCommitted);
        this.UpdateCommitted(this, e);
    }
    CallOnceRepositoryActions();
}

	public event RepositoryEventHandler PreDelete;

public virtual void OnPreDelete()
{
    if (this.PreDelete != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.PreDelete);
        this.PreDelete(this, e);
    }

    var tip = ObjectLogableAttribute.GetTip(this.ObjectType);
}

	public event RepositoryEventHandler Deleted;

public virtual void OnDeleted()
{
    if (this.Deleted != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.Deleted);
        this.Deleted(this, e);
    }
    CallOnceRepositoryActions();
}

	public event RepositoryEventHandler DeletePreCommit;

public virtual void OnDeletePreCommit()
{
    if (this.DeletePreCommit != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.DeletePreCommit);
        this.DeletePreCommit(this, e);
    }
    CallOnceRepositoryActions();
}

/// <summary>
/// 对象被真实提交到仓储删除后
/// </summary>
	public event RepositoryEventHandler DeleteCommitted;

public virtual void OnDeleteCommitted()
{
    if (this.DeleteCommitted != null)
    {
        var e = new RepositoryEventArgs(this, StatusEventType.DeleteCommitted);
        this.DeleteCommitted(this, e);
    }
    CallOnceRepositoryActions();
}

	#region 仓储操作回调

	private List<Action> _onceRepositoryCallbackActions = null;

/// <summary>
/// 在下次执行完该对象的仓储操作后执行<paramref name="action" />动作
/// 该动作仅被执行一次
/// </summary>
/// <param name="action"></param>
	protected void OnceRepositoryCallback(Action action) {
		if (_onceRepositoryCallbackActions == null)
			_onceRepositoryCallbackActions = new List<Action>();
		_onceRepositoryCallbackActions.Add(action);
	}

private void CallOnceRepositoryActions()
{
    if (_onceRepositoryCallbackActions == null) return;
    foreach (var action in _onceRepositoryCallbackActions) action();
    _onceRepositoryCallbackActions.Clear(); //执行完后清空行为集合
}

//	region 内聚根可以具有远程能力

	private boolean _remotable;

	public boolean remotable() {
		return _remotable;
	}

/// <summary>
/// 初始化对象的远程能力
/// </summary>
	private void InitRemotable() {
		if (this.RemotableTip != null) {
			// 指示了对象具备远程能力
			this.UpdateCommitted += NotifyUpdated;
			this.DeleteCommitted += NotifyDeleted;
		}
	}

	private void NotifyUpdated(object sender, RepositoryEventArgs e) {
		RemotePortal.NotifyUpdated(this.RemoteType, e.Target.GetIdentity());
	}

	private void NotifyDeleted(object sender, RepositoryEventArgs e) {
		RemotePortal.NotifyDeleted(this.RemoteType, e.Target.GetIdentity());
	}

	#endregion

	private static RemotableAttribute _remotableTip;

	static()
	{
		var objectType = typeof(TObject);
		_remotableTip = RemotableAttribute.GetTip(objectType);
	}
}
