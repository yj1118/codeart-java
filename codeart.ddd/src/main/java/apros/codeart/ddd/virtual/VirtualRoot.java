package apros.codeart.ddd.virtual;

import apros.codeart.ddd.*;
import apros.codeart.ddd.internal.AggregateRootEventManager;
import apros.codeart.ddd.metadata.internal.MetadataLoader;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.RepositoryEventArgs;
import apros.codeart.ddd.repository.RepositoryRollbackEventArgs;
import apros.codeart.util.EventHandler;

@MergeDomain
@FrameworkDomain
public class VirtualRoot extends VirtualEntity implements IAggregateRoot {

    private final AggregateRootEventManager _eventManager;

    public VirtualRoot(boolean isEmpty) {
        super(isEmpty);
        _eventManager = new AggregateRootEventManager(this);
        this.onConstructed();
    }

    @ConstructorRepository
    public VirtualRoot() {
        super();
        _eventManager = new AggregateRootEventManager(this);
        this.onConstructed();
    }

    private String _uniqueKey;

    public String uniqueKey() {
        if (_uniqueKey == null) {
            _uniqueKey = UniqueKeyCalculator.getUniqueKey(this);
        }
        return _uniqueKey;
    }

    /**
     * 仓储操作回滚事件
     */
    public EventHandler<RepositoryRollbackEventArgs> rollback() {
        return _eventManager.rollback();
    }

    public void onRollback(Object sender, RepositoryRollbackEventArgs e) {
        _eventManager.onRollback(sender, e);
    }

    public EventHandler<RepositoryEventArgs> preAdd() {
        return _eventManager.preAdd();
    }

    public void onPreAdd() {
        _eventManager.onPreAdd();
    }

    public EventHandler<RepositoryEventArgs> added() {
        return _eventManager.added();
    }

    public void onAdded() {
        _eventManager.onAdded();
    }

    public EventHandler<RepositoryEventArgs> addPreCommit() {
        return _eventManager.addPreCommit();
    }

    public void onAddPreCommit() {
        _eventManager.onAddPreCommit();
    }

    public EventHandler<RepositoryEventArgs> addCommitted() {
        return _eventManager.addCommitted();
    }

    public void onAddCommitted() {
        _eventManager.onAddCommitted();
    }

    public EventHandler<RepositoryEventArgs> preUpdate() {
        return _eventManager.preUpdate();
    }

    public void onPreUpdate() {
        _eventManager.onPreUpdate();
    }

    public EventHandler<RepositoryEventArgs> updated() {
        return _eventManager.updated();
    }

    public void onUpdated() {
        _eventManager.onUpdated();
    }

    public EventHandler<RepositoryEventArgs> updatePreCommit() {
        return _eventManager.updatePreCommit();
    }

    public void onUpdatePreCommit() {
        _eventManager.onUpdatePreCommit();
    }

    public EventHandler<RepositoryEventArgs> updateCommitted() {
        return _eventManager.updateCommitted();
    }

    public void onUpdateCommitted() {
        _eventManager.onUpdateCommitted();
    }

    public EventHandler<RepositoryEventArgs> preDelete() {
        return _eventManager.preDelete();
    }

    public void onPreDelete() {
        _eventManager.onPreDelete();
    }

    public EventHandler<RepositoryEventArgs> deleted() {
        return _eventManager.deleted();
    }

    public void onDeleted() {
        _eventManager.onDeleted();
    }

    public EventHandler<RepositoryEventArgs> deletePreCommit() {
        return _eventManager.deletePreCommit();
    }

    public void onDeletePreCommit() {
        _eventManager.onDeletePreCommit();
    }

    public EventHandler<RepositoryEventArgs> deleteCommitted() {
        return _eventManager.deleteCommitted();
    }

    public void onDeleteCommitted() {
        _eventManager.onDeleteCommitted();
    }

//	public Iterable<(
//
//	string Type, string Id)>
//
//	GetObservers()
//			{
//			    return Array.Empty<(string Type, string Id)>();
//			}
//
//	public static DynamicRoot CreateEmpty<RT>()
//	where RT:TypeDefine
//	{
//			    var type = TypeDefine.GetDefine<RT>();
//			    return new DynamicRoot(type, true);
//			}

//    private static final VirtualRoot EmptyInstance = new VirtualRoot(true);

    public static VirtualRoot empty(String virtualObjectName) {
        var meta = ObjectMetaLoader.get(virtualObjectName);
        return (VirtualRoot) DomainObject.getEmpty(meta.objectType());
    }

}
