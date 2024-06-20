package apros.codeart.ddd.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.StatusEvent;
import apros.codeart.ddd.StatusEventType;
import apros.codeart.ddd.ValidationException;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.ddd.cqrs.master.Forker;
import apros.codeart.ddd.repository.access.DataAccess;
import apros.codeart.ddd.repository.access.DataConnection;
import apros.codeart.i18n.Language;
import apros.codeart.util.EventHandler;
import apros.codeart.util.ListUtil;

import static apros.codeart.runtime.Util.propagate;

public class DataContext implements IDataContext {

    private DataContext() {
        initializeTransaction();
    }

    //region 镜像

    private ArrayList<IAggregateRoot> _mirrors;
    private boolean _lockedMirrors = false;

    private void disposeMirror() {
        if (_mirrors != null)
            _mirrors.clear();
        _lockedMirrors = false;
    }

    private void addMirror(IAggregateRoot obj) {
        if (isCommiting())
            throw new DataContextException(Language.strings("apros.codeart.ddd","CanNotAddMirror"));

        if (_mirrors == null)
            _mirrors = new ArrayList<IAggregateRoot>();

        if (ListUtil.contains(_mirrors, (t) -> {
            return t.uniqueKey().equals(obj.uniqueKey());
        }))
            return;
        _mirrors.add(obj);
    }

    private void lockMirrors() {
        if (_lockedMirrors)
            return; // 不重复锁定镜像对象
        LockManager.lock(_mirrors);
        _lockedMirrors = true;
    }

    /**
     * 判定对象是否为镜像（判断obj是否在_mirrors中）
     *
     * @param obj
     * @return
     */
    boolean isMirror(Object obj) {
        if (_mirrors == null)
            return false;
        return ListUtil.contains(_mirrors, (t) -> {
            // == 用于基本数据类型时比较的是值，用于对象引用时比较的是两个引用是否指向堆内存中的同一个对象位置。
            // 这与 .NET 中的 Object.ReferenceEquals 方法的作用相似。
            return t == obj;
        });
    }

    //endregion

    //region 当前被加载的对象集合

    private DomainBufferImpl _buffer;

    private void disposeBuffer() {
        if (_buffer != null)
            _buffer.clear();
    }

    public void addBuffer(IAggregateRoot obj, boolean isMirror) {

        if (_buffer == null)
            _buffer = new DomainBufferImpl();

        _buffer.add(obj);

        if (isMirror)
            addMirror(obj);
    }

    public void removeBuffer(Class<?> objectType, Object id) {

        if (_buffer == null)
            return;

        _buffer.remove(objectType, id);
    }

    public IAggregateRoot obtainBuffer(Class<?> objectType, Object id, Supplier<IAggregateRoot> load,
                                       boolean isMirror) {

        if (_buffer == null)
            _buffer = new DomainBufferImpl();

        var obj = _buffer.obtain(objectType, id, load);

        if (isMirror)
            addMirror(obj);

        return obj;
    }

    /**
     * 获取数据上下文中存放的缓冲对象
     *
     * @return
     */
    Iterable<IAggregateRoot> getBufferObjects() {
        return _buffer.items();
    }

//	region CUD

    public <T extends IAggregateRoot> void registerAdded(T item, IPersistRepository repository) {
        this.openDelayMode();
        processAction(new ScheduledAction(item, repository, ScheduledActionType.Create));
        item.saveState();
        item.markClean();// 无论是延迟执行，还是立即执行，我们都需要提供统一的状态给领域层使用
    }

    public <T extends IAggregateRoot> void registerUpdated(T item, IPersistRepository repository) {
        this.openDelayMode();
        processAction(new ScheduledAction(item, repository, ScheduledActionType.Update));
        item.saveState();
        item.markClean();// 无论是延迟执行，还是立即执行，我们都需要提供统一的状态给领域层使用
    }

    public <T extends IAggregateRoot> void registerDeleted(T item, IPersistRepository repository) {
        this.openDelayMode();
        processAction(new ScheduledAction(item, repository, ScheduledActionType.Delete));
        item.saveState();
        item.markDirty();// 无论是延迟执行，还是立即执行，我们都需要提供统一的状态给领域层使用
    }

//	region 锁

    public void openLock(QueryLevel level) {
        if (level.equals(QueryLevel.HOLD) || level.equals(QueryLevel.SINGLE)){
            openTimelyMode();
            return;
        }

        if(level.equals(QueryLevel.SHARE)){
            openShareMode();
            return;
        }

    }

    //region 执行计划

    private ArrayList<ScheduledAction> _actions;

    private boolean hasActions() {
        return _actions != null && !_actions.isEmpty();
    }

    private ArrayList<ScheduledAction> actions() {
        if (_actions == null)
            _actions = new ArrayList<ScheduledAction>();
        return _actions;
    }

    private void disposeSchedule() {
        if (_actions != null) {
            _actions.clear();
            _actions = null;
        }

    }

    private void processAction(ScheduledAction action) {

        if (this._transactionStatus == TransactionStatus.Delay) {
            actions().add(action);// 若处于延迟模式的事务中，那么将该操作暂存
            return;
        }

        if (this._transactionStatus == TransactionStatus.Timely) {
            // 若已经开启全局事务，直接执行
            actions().add(action); // 直接执行也要加入到actions集合中
            executeAction(action);
            return;
        }

        // 以下代码不会有执行的机会，因为addActiond的时候，就会提升status的级别
//        if (this._transactionStatus == TransactionStatus.None) {
//            // 没有开启事务，立即执行
//            _conn.begin(this._transactionStatus);
//
//            executeAction(action);
//            raisePreCommit(action);
//
//            _conn.commit();
//            raiseCommitted(action);
//
//            return;
//        }
    }

    private void raisePreCommit(ScheduledAction action) {
        switch (action.type()) {
            case ScheduledActionType.Create:
                action.target().onAddPreCommit();
                statusEventExecute(StatusEventType.AddPreCommit, action.target());
                Forker.notifyAdd(action.target());
                break;
            case ScheduledActionType.Update:
                action.target().onUpdatePreCommit();
                statusEventExecute(StatusEventType.UpdatePreCommit, action.target());
                Forker.notifyUpdate(action.target());
                break;
            case ScheduledActionType.Delete:
                action.target().onDeletePreCommit();
                statusEventExecute(StatusEventType.DeletePreCommit, action.target());
                Forker.notifyDelete(action.target());
                break;
        }
    }

    private void statusEventExecute(StatusEventType type, IAggregateRoot target) {
        StatusEvent.execute(type, target);
    }

    private void raiseCommitted(ScheduledAction action) {
        switch (action.type()) {
            case ScheduledActionType.Create:
                action.target().onAddCommitted();
                action.repository().onAddCommited(action.target());
                statusEventExecute(StatusEventType.AddCommitted, action.target());
                break;
            case ScheduledActionType.Update:
                action.target().onUpdateCommitted();
                action.repository().onUpdateCommited(action.target());
                statusEventExecute(StatusEventType.UpdateCommitted, action.target());
                break;
            case ScheduledActionType.Delete:
                action.target().onDeleteCommitted();
                action.repository().onDeleteCommited(action.target());
                statusEventExecute(StatusEventType.DeleteCommitted, action.target());
                break;
        }
    }

    private void raisePreCommitQueue() {
        if (hasActions()) {
            for (var action : _actions) {
                raisePreCommit(action);
            }
        }
    }

    private void raiseCommittedQueue() {
        if (hasActions()) {
            for (var action : _actions) {
                raiseCommitted(action);
            }
        }
    }

    /**
     * 检验执行的计划
     */
    private void validateAction(ScheduledAction action) {
        if (action.target().isEmpty())
            throw new ActionTargetIsEmptyException(Language.strings("apros.codeart.ddd", "EmptyObjectNotRepository",
                    action.target().getClass().getName()));

        if (action.type() == ScheduledActionType.Delete)
            return; // 删除操作，不需要验证固定规则

        ValidationResult result = action.validate();
        if (!result.isSatisfied())
            throw new ValidationException(result);
    }

    /**
     * 执行计划
     *
     * @param action
     */
    private void executeAction(ScheduledAction action) {
        if (action.expired())
            return;

        action.target().loadState();
        this.validateAction(action);

        var repository = action.repository();
        switch (action.type()) {
            case ScheduledActionType.Create:
                repository.persistAdd(action.target());
                action.target().markClean();
                break;
            case ScheduledActionType.Update:
                repository.persistUpdate(action.target());
                action.target().markClean();
                break;
            case ScheduledActionType.Delete:
                repository.persistDelete(action.target());
                action.target().markDirty();
                break;
        }

        action.markExpired();
    }

    //region 事务管理

    private TransactionStatus _transactionStatus;

    public TransactionStatus transactionStatus() {
        return _transactionStatus;
    }


    private int _transactionCount;

    private DataConnection _conn;

    private boolean _isCommiting;

    /**
     * 是否正在执行提交操作
     */
    public boolean isCommiting() {
        return _isCommiting;
    }

    public DataConnection connection() {
        return _conn;
    }

    private void initializeTransaction() {
        _transactionStatus = TransactionStatus.None;
        _transactionCount = 0;
        _isCommiting = false;
        _conn = new DataConnection();
    }

    private void disposeTransaction() {
        _transactionStatus = TransactionStatus.None;
        _transactionCount = 0;
        _isCommiting = false;
        if (_conn != null) {
            _conn.close();
        }

        if (_committed != null)
            _committed.clear();
    }

    public boolean inTransaction() {
        return _transactionCount > 0;
    }

    private void openDelayMode() {
        if (_transactionStatus == TransactionStatus.None
                || _transactionStatus == TransactionStatus.Share) {
            _transactionStatus = TransactionStatus.Delay;
        }
    }

    private void openShareMode(){

        if(_transactionStatus == TransactionStatus.Share) return;
        if(_transactionStatus == TransactionStatus.Delay){
            throw new DataContextException(Language.strings("apros.codeart.ddd","NotSupportTranTo","Delay","Share"));
        }

        if(_transactionStatus == TransactionStatus.Timely){
            throw new DataContextException(Language.strings("apros.codeart.ddd","NotSupportTranTo","Timely","Share"));
        }

        if (!this.inTransaction()) {
            throw new NotBeginTransactionException();
        }

        if (_transactionStatus == TransactionStatus.None) {
            _transactionStatus = TransactionStatus.Share;
            _conn.begin(_transactionStatus);
        }
    }

    /**
     * 开启即时事务,并且锁定事务
     */
    public void openTimelyMode() {

        if(_transactionStatus == TransactionStatus.Timely) return;
        if(_transactionStatus == TransactionStatus.Share){
            throw new DataContextException(Language.strings("apros.codeart.ddd","NotSupportTranTo","Share","Timely"));
        }

        if (!this.inTransaction()) {
            // 有对持久层的增改删的操作，但是没有开启事务
            throw new NotBeginTransactionException();
        }

        // 开启即时事务
        this._transactionStatus = TransactionStatus.Timely;

        _conn.begin(this._transactionStatus);

        if (!isCommiting()) {
            // 没有之前的队列要执行
            // 在提交时更改了事务模式,只有可能是在验证行为时发生，该队列会在稍后立即执行，因此此处不执行队列
            executeActionQueue();
        }
    }

    /**
     * 开启事务BeginTransaction和提交事务Commit必须成对出现
     */
    public void beginTransaction() {
        _transactionCount++;
    }

    public void commit() throws Exception {

        if (hasActions() && !this.inTransaction()) {
            // 有对持久层的增改删的操作，但是没有开启事务
            throw new NotBeginTransactionException();
        }

       try{
           _transactionCount--;
           if (_transactionCount == 0) {
               if (isCommiting())
                   throw new RepeatedCommitException();

               _isCommiting = true;

               if (_transactionStatus == TransactionStatus.Delay) {
                   _transactionStatus = TransactionStatus.Timely; // 开启即时事务

                   _conn.begin(_transactionStatus);
                   executeActionQueue();
                   raisePreCommitQueue();
                   _conn.commit();

                   raiseCommittedQueue();
               } else if (_transactionStatus == TransactionStatus.Timely) {
                   executeActionQueue();
                   raisePreCommitQueue();

                   _conn.commit();

                   raiseCommittedQueue();
               } else if (_transactionStatus == TransactionStatus.Share) {
                   _conn.commit();
                   //为None和Share的状态不用执行，因为没有actions，只有查询，已经执行了
               }

               this.onCommitted();
               clear();
           }
       } catch (Exception ex){
           // 注意，由于提交失败了，所以这里_transactionCount要恢复+1
           // 这样外部就知道是处在提交事务中失败的
           _transactionCount++;
           throw ex;
       }
    }

    private EventHandler<DataContextEventArgs> _committed;

    public EventHandler<DataContextEventArgs> committed() {
        if (_committed == null)
            _committed = new EventHandler<DataContextEventArgs>();
        return _committed;
    }

    private void onCommitted() {
        if (_committed != null) {
            _committed.raise(this, () -> {
                return DataContextEventArgs.Instance;
            });
        }
    }

    private void executeActionQueue() {
        // 执行行为队列之前，我们会对镜像进行锁定
        lockMirrors();
        if (hasActions()) {
            for (ScheduledAction action : _actions) {
                this.executeAction(action);
            }
        }
    }

    public boolean isDirty() {
        return _actions != null && !_actions.isEmpty();
    }

    /**
     * 清理资源
     */
    void clear() {
        disposeSchedule();
        disposeRollback();
        disposeTransaction();
        disposeMirror();
        disposeBuffer();
        disposeItems();
    }

    @Override
    public void close() throws Exception {
        if (this.inTransaction()) {
            this.rollback();
        } else {
            clear();
        }
    }

    private RollbackCollection _rollbacks;

    private void disposeRollback() {
        if (_rollbacks != null)
            _rollbacks.clear();
    }

    public void rollback() {
        if (_rollbacks == null)
            return;
        try {
            _rollbacks.execute(this);
            raiseRolledBack(this);
        } finally {
            clear();
        }
    }

    public void registerRollback(RepositoryRollbackEventArgs e) {
        if (_rollbacks == null)
            _rollbacks = new RollbackCollection();
        _rollbacks.add(e);
    }

    public static final EventHandler<RolledBackEventArgs> RolledBack = new EventHandler<RolledBackEventArgs>();

    private static void raiseRolledBack(DataContext context) {
        RolledBack.raise(context, () -> new RolledBackEventArgs(context));
    }

    //region 额外项

    private Map<String, Object> _items = null;

    public void setItem(String name, Object item) {
        if (_items == null)
            _items = new HashMap<String, Object>();
        _items.put(name, item);
    }

    public Object getItem(String name) {
        if (_items == null)
            return null;
        return _items.get(name);
    }

    public boolean hasItem(String name) {
        if (_items == null)
            return false;
        return _items.containsKey(name);
    }

    private void disposeItems() {
        if (_items != null) {
            _items.clear();
            _items = null;
        }
    }

    //endregion

    //region 基于当前应用程序会话的数据上下文

    private static final String _sessionKey = "DataContext.Current";

    public static DataContext getCurrent() {
        DataContext dataContext = AppSession.getItem(_sessionKey);
        if (dataContext == null)
            throw new IllegalStateException(Language.strings("DataContextNull"));
        return dataContext;
    }

    static void setCurrent(DataContext context) {
        AppSession.setItem(_sessionKey, context);
    }

    public static boolean existCurrent() {
        return AppSession.getItem(_sessionKey) != null;
    }

    //region 无返回值

    private static void using(DataContext dataContext, Consumer<DataConnection> action, boolean timely) {
        try {
            boolean isCommiting = dataContext.isCommiting();
            if (isCommiting) {
                // 事务上下文已进入提交阶段，那么不必重复开启事务
                action.accept(dataContext.connection());
            } else {
                dataContext.beginTransaction();
                if (timely)
                    dataContext.openTimelyMode();

                action.accept(dataContext.connection());
                dataContext.commit();
            }
        } catch (Exception ex) {
            if (dataContext.inTransaction()) {
                dataContext.rollback();
            }
            throw propagate(ex);
        }
    }

    public static void using(Runnable action) {
        using(action, false);
    }

    public static void using(Runnable action, boolean timely) {
        using((access) -> {
            action.run();
        }, timely);
    }

    public static void using(Consumer<DataConnection> action) {
        using(action, false);
    }

    public static void using(Consumer<DataConnection> action, boolean timely) {
        if (DataContext.existCurrent()) {
            var dataContext = DataContext.getCurrent();
            using(dataContext, action, timely);
        } else {

            var dataContext = new DataContext();
            DataContext.setCurrent(dataContext);
            try {
                using(dataContext, action, timely);
            } catch (Exception e) {
                throw e;
            } finally {
                DataContext.setCurrent(null);// 执行完后，释放
            }
        }

    }

    //endregion

    //region 有返回值

    public static <T> T using(Supplier<T> action) {
        return using(action, false);
    }

    public static <T> T using(Supplier<T> action, boolean timely) {
        return using((access) -> {
            return action.get();
        }, timely);
    }

    public static <T> T using(Function<DataConnection, T> action) {
        return using(action, false);
    }

    public static <T> T using(Function<DataConnection, T> action, boolean timely) {
        if (DataContext.existCurrent()) {
            var dataContext = DataContext.getCurrent();
            return using(dataContext, action, timely);
        } else {

            var dataContext = new DataContext();
            DataContext.setCurrent(dataContext);
            try {
                return using(dataContext, action, timely);
            } catch (Exception e) {
                throw e;
            } finally {
                DataContext.setCurrent(null);// 执行完后，释放
            }
        }

    }

    private static <T> T using(DataContext dataContext, Function<DataConnection, T> action, boolean timely) {
        try {
            T result;
            boolean isCommiting = dataContext.isCommiting();
            if (isCommiting) {
                // 事务上下文已进入提交阶段，那么不必重复开启事务
                result = action.apply(dataContext.connection());
            } else {
                dataContext.beginTransaction();
                if (timely)
                    dataContext.openTimelyMode();

                result = action.apply(dataContext.connection());
                dataContext.commit();
            }
            return result;
        } catch (Exception ex) {
            if (dataContext.inTransaction()) {
                dataContext.rollback();
            }
            throw propagate(ex);
        }
    }

    //endregion

    public static void newScope(Runnable action) {
        newScope((conn) -> {
            action.run();
        });
    }

    public static <T> T newScope(Supplier<T> action) {
        return newScope((conn) -> {
            return action.get();
        });
    }

    /**
     * 新建一个范围数据上下文，该数据上下文会创建一个新的独立事务
     *
     * @param action
     */
    public static void newScope(Consumer<DataConnection> action) {
        DataContext prev = null;
        if (DataContext.existCurrent()) {
            prev = DataContext.getCurrent(); // 保留当前的数据上下文对象
        }

        try {
            var dataContext = new DataContext();
            DataContext.setCurrent(dataContext);
            using(dataContext, action, true);

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (prev != null) {
                DataContext.setCurrent(prev); // 还原当前数据上下文
            } else {
                DataContext.setCurrent(null);
            }
        }
    }

    public static <T> T newScope(Function<DataConnection, T> action) {
        DataContext prev = null;
        if (DataContext.existCurrent()) {
            prev = DataContext.getCurrent(); // 保留当前的数据上下文对象
        }

        try {
            var dataContext = new DataContext();
            DataContext.setCurrent(dataContext);
            return using(dataContext, action, true);

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (prev != null) {
                DataContext.setCurrent(prev); // 还原当前数据上下文
            } else {
                DataContext.setCurrent(null);
            }
        }
    }

    //endregion

}
