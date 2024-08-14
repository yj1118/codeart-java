package apros.codeart.ddd.repository;

import apros.codeart.TestSupport;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IRepository;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.StatusEvent;
import apros.codeart.ddd.StatusEventType;
import apros.codeart.runtime.TypeUtil;

public abstract class AbstractRepository<TRoot extends IAggregateRoot> extends PersistRepository
        implements IRepository<TRoot> {

    private Class<? extends TRoot> _rootType;

    public Class<? extends TRoot> rootType() {
        if (_rootType == null) {
            _rootType = getRootTypeImpl();
        }
        return _rootType;
    }

    public AbstractRepository() {
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends TRoot> getRootTypeImpl() {
        return (Class<? extends TRoot>) TypeUtil.getActualType(this.getClass())[0];
    }

    // #region 增加数据

    protected void registerAdded(IAggregateRoot obj) {
        DataContext.getCurrent().registerAdded(obj, this);
    }

    protected void registerRollbackAdd(IAggregateRoot obj) {
        var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Add);
        DataContext.getCurrent().registerRollback(args);
    }

    @SuppressWarnings("unchecked")
    public final TRoot find(Object id, QueryLevel level) {
        return (TRoot) findRoot(id, level);
    }

    @SuppressWarnings("unchecked")
    public final void addRoot(IAggregateRoot obj) {
        add((TRoot) obj);
    }

    public final void add(TRoot obj) {
        if (obj.isEmpty())
            return;

        DataContext.using(() -> {
            registerRollbackAdd(obj);
//            StatusEvent.execute(StatusEventType.PreAdd, obj);
//            obj.onPreAdd();
            registerAdded(obj);
//            obj.onAdded();
//            StatusEvent.execute(StatusEventType.Added, obj);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void persistAdd(IAggregateRoot obj) {
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

    protected final void registerRollbackUpdate(IAggregateRoot obj) {
        var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Update);
        DataContext.getCurrent().registerRollback(args);
    }

    protected final void registerUpdated(IAggregateRoot obj) {
        DataContext.getCurrent().registerUpdated(obj, this);
    }

    @SuppressWarnings("unchecked")
    public final void updateRoot(IAggregateRoot obj) {
        update((TRoot) obj);
    }

    public final void update(TRoot obj) {
        if (obj.isEmpty())
            return;

        DataContext.using(() -> {
            registerRollbackUpdate(obj);
//            StatusEvent.execute(StatusEventType.PreUpdate, obj);
//            obj.onPreUpdate();
            registerUpdated(obj);
//            obj.onUpdated();
//            StatusEvent.execute(StatusEventType.Updated, obj);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void persistUpdate(IAggregateRoot obj) {
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

    protected final void registerRollbackDelete(IAggregateRoot obj) {
        var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Delete);
        DataContext.getCurrent().registerRollback(args);
    }

    protected final void registerDeleted(IAggregateRoot obj) {
        DataContext.getCurrent().registerDeleted(obj, this);
    }

    @SuppressWarnings("unchecked")
    public final void deleteRoot(IAggregateRoot obj) {
        delete((TRoot) obj);
    }

    public final void delete(TRoot obj) {
        if (obj.isEmpty())
            return;

        DataContext.using(() -> {
            registerRollbackDelete(obj);
//            StatusEvent.execute(StatusEventType.PreDelete, obj);
//            obj.onPreDelete();
            registerDeleted(obj);
//            obj.onDeleted();
//            StatusEvent.execute(StatusEventType.Deleted, obj);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void persistDelete(IAggregateRoot obj) {
        if (obj.isEmpty())
            return;
        TRoot root = (TRoot) obj;
        if (!root.isEmpty()) {
            if (this.onPrePersist(obj, RepositoryAction.Delete)) {
                persistDeleteRoot(root);
            }
            this.onPersisted(obj, RepositoryAction.Delete);
        }
    }

    protected abstract void persistDeleteRoot(TRoot obj);

    /**
     * 仓储的初始化操作
     */
    public void init() {
        //默认的情况下不需要额外的初始化操作
    }


    /**
     * 清理操作，主要是为了提供给测试使用
     */
    @TestSupport
    public void clearUp() {
        //默认的情况下不需要额外的清理操作
    }


}
