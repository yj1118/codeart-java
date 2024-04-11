package com.apros.codeart.ddd.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.apros.codeart.context.AppSession;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.StatusEvent;
import com.apros.codeart.ddd.StatusEventType;
import com.apros.codeart.ddd.ValidationException;
import com.apros.codeart.ddd.ValidationResult;
import com.apros.codeart.ddd.repository.access.DataAccess;
import com.apros.codeart.ddd.repository.access.DataConnection;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.EventHandler;
import com.apros.codeart.util.ListUtil;

public class DataContext implements IDataContext {

	private DataContext() {
		initializeTransaction();
	}

//	#region 镜像

	private ArrayList<IAggregateRoot> _mirrors;
	private boolean _lockedMirrors = false;

	private void disposeMirror() {
		if (_mirrors != null)
			_mirrors.clear();
		_lockedMirrors = false;
	}

	private void addMirror(IAggregateRoot obj) {
		if (isCommiting())
			throw new DataContextException(Language.strings("CanNotAddMirror"));

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

//	region 当前被加载的对象集合

	private ArrayList<IAggregateRoot> _buffer;

	private void disposeBuffer() {
		if (_buffer != null)
			_buffer.clear();
	}

	public void addBuffer(IAggregateRoot obj, boolean isMirror) {

		if (_buffer == null)
			_buffer = new ArrayList<IAggregateRoot>();

		if (ListUtil.contains(_buffer, (t) -> {
			return t.uniqueKey().equals(obj.uniqueKey());
		}))
			return;
		_buffer.add(obj);

		if (isMirror)
			addMirror(obj);
	}

	/**
	 * 获取数据上下文中存放的缓冲对象
	 * 
	 * @return
	 */
	Iterable<IAggregateRoot> getBufferObjects() {
		if (_buffer == null)
			return ListUtil.empty();
		return _buffer;
	}

//	region CUD

	public <T extends IAggregateRoot> void registerAdded(T item, IPersistRepository repository) {
		processAction(new ScheduledAction(item, repository, ScheduledActionType.Create));
		item.saveState();
		item.markClean();// 无论是延迟执行，还是立即执行，我们都需要提供统一的状态给领域层使用
	}

	public <T extends IAggregateRoot> void registerUpdated(T item, IPersistRepository repository) {
		processAction(new ScheduledAction(item, repository, ScheduledActionType.Update));
		item.saveState();
		item.markClean();// 无论是延迟执行，还是立即执行，我们都需要提供统一的状态给领域层使用
	}

	public <T extends IAggregateRoot> void registerDeleted(T item, IPersistRepository repository) {
		processAction(new ScheduledAction(item, repository, ScheduledActionType.Delete));
		item.saveState();
		item.markDirty();// 无论是延迟执行，还是立即执行，我们都需要提供统一的状态给领域层使用
	}

//	region 锁

	public void openLock(QueryLevel level) {
		if (isLockQuery(level))
			openTimelyMode();
	}

	private boolean isLockQuery(QueryLevel level) {
		return level.equals(QueryLevel.HoldSingle) || level.equals(QueryLevel.Single) || level.equals(QueryLevel.Share);
	}

//	region 领域对象查询服务

	/**
	 * 向数据上下文注册查询，该方法会控制锁和同步查询结果
	 */
	public <T extends IAggregateRoot> T registerQueried(Class<T> objectType, QueryLevel level,
			Supplier<T> persistQuery) {
		this.openLock(level);
		return persistQuery.get();
	}

	/**
	 * 向数据上下文注册集合查询，该方法会控制锁和同步查询结果
	 */
	public <T extends IAggregateRoot> Iterable<T> registerCollectionQueried(Class<T> objectType, QueryLevel level,
			Supplier<Iterable<T>> persistQuery) {
		this.openLock(level);
		return persistQuery.get();
	}

	/**
	 * 向数据上下文注册查询，该方法会控制锁和同步查询结果
	 */
	public <T extends IAggregateRoot> Page<T> registerPageQueried(Class<T> objectType, QueryLevel level,
			Supplier<Page<T>> persistQuery) {
		this.openLock(level);
		return persistQuery.get();
	}

//	region 执行计划

	private ArrayList<ScheduledAction> _actions;

	private ArrayList<ScheduledAction> actions() {
		if (_actions == null)
			_actions = new ArrayList<ScheduledAction>();
		return _actions;
	}

	private void disposeSchedule() {
		if (_actions != null)
			_actions.clear();
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

		if (this._transactionStatus == TransactionStatus.None) {
			// 没有开启事务，立即执行
			_conn.begin();

			executeAction(action);
			raisePreCommit(action);

			_conn.commit();
			raiseCommitted(action);

			return;
		}
	}

	private void raisePreCommit(ScheduledAction action) {
		switch (action.type()) {
		case ScheduledActionType.Create:
			action.target().onAddPreCommit();
			statusEventExecute(StatusEventType.AddPreCommit, action.target());
			break;
		case ScheduledActionType.Update:
			action.target().onUpdatePreCommit();
			statusEventExecute(StatusEventType.UpdatePreCommit, action.target());
			break;
		case ScheduledActionType.Delete:
			action.target().onDeletePreCommit();
			statusEventExecute(StatusEventType.DeletePreCommit, action.target());
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
		for (var action : _actions) {
			raisePreCommit(action);
		}
	}

	private void raiseCommittedQueue() {
		for (var action : _actions) {
			raiseCommitted(action);
		}
	}

	/**
	 * 检验执行的计划
	 * 
	 * @param action
	 */
	private void validateAction(ScheduledAction action) {
		if (action.target().isEmpty())
			throw new ActionTargetIsEmptyException(
					Language.strings("codeart.ddd", "EmptyObjectNotRepository", action.target().getClass().getName()));

		if (action.type() == ScheduledActionType.Delete)
			return; // 删除操作，不需要验证固定规则

		ValidationResult result = action.validate();
		if (!result.isSatisfied())
			throw new ValidationException(result);
	}

	/// <summary>
	/// 执行计划
	/// </summary>
	/// <param name="action"></param>
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

//	region 事务管理

	private TransactionStatus _transactionStatus;
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
	}

	public boolean inTransaction() {
		return _transactionStatus != TransactionStatus.None;
	}

	/**
	 * 开启即时事务,并且锁定事务
	 */
	public void openTimelyMode() {
		if (_transactionStatus != TransactionStatus.Timely) {
			if (!this.inTransaction())
				throw new NotBeginTransactionException();

			// 开启即时事务
			this._transactionStatus = TransactionStatus.Timely;

			_conn.begin();

			if (!isCommiting()) {
				// 没有之前的队列要执行
				// 在提交时更改了事务模式,只有可能是在验证行为时发生，该队列会在稍后立即执行，因此此处不执行队列
				executeActionQueue();
			}
		}
	}

	/**
	 * 开启事务BeginTransaction和提交事务Commit必须成对出现
	 */
	public void beginTransaction() {
		if (this.inTransaction()) {
			_transactionCount++;
		} else {
			_transactionStatus = TransactionStatus.Delay;
			_actions.clear();
			_transactionCount++;
			_conn.initialize();
		}
	}

	public void commit() {
		if (!this.inTransaction())
			throw new NotBeginTransactionException();
		else {
			_transactionCount--;
			if (_transactionCount == 0) {

				if (isCommiting())
					throw new RepeatedCommitException();

				_isCommiting = true;

				try {
					if (_transactionStatus == TransactionStatus.Delay) {
						_transactionStatus = TransactionStatus.Timely; // 开启即时事务

						_conn.begin();
						executeActionQueue();
						raisePreCommitQueue();
						_conn.commit();

						raiseCommittedQueue();
					} else if (_transactionStatus == TransactionStatus.Timely) {
						executeActionQueue();
						raisePreCommitQueue();

						_conn.commit();

						raiseCommittedQueue();
					}
				} catch (Exception ex) {
					throw ex;
				} finally {
					clear();
					_isCommiting = false;
				}
			}
		}
	}

	private void executeActionQueue() {
		// 执行行为队列之前，我们会对镜像进行锁定
		lockMirrors();
		if (_actions != null) {
			for (ScheduledAction action : _actions) {
				this.executeAction(action);
			}
		}
	}

	public boolean isDirty() {
		return _actions.size() > 0;
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
		if (!this.inTransaction())
			throw new NotBeginTransactionException();
		else {
			if (_rollbacks == null)
				return;
			try {
				_rollbacks.execute(this);
				raiseRolledBack(this);
			} catch (Exception ex) {
				throw ex;
			} finally {
				clear();
			}
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

//	region 额外项

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

//	region 基于当前应用程序会话的数据上下文

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

	private static void using(DataContext dataContext, Consumer<DataAccess> action, boolean timely) {
		try {
			boolean isCommiting = dataContext.isCommiting();
			if (isCommiting) {
				// 事务上下文已进入提交阶段，那么不必重复开启事务
				action.accept(dataContext.connection().access());
			} else {
				dataContext.beginTransaction();
				if (timely)
					dataContext.openTimelyMode();

				action.accept(dataContext.connection().access());
				dataContext.commit();
			}
		} catch (Exception ex) {
			if (dataContext.inTransaction()) {
				dataContext.rollback();
			}
			throw ex;
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

	public static void using(Consumer<DataAccess> action) {
		using(action, false);
	}

	public static void using(Consumer<DataAccess> action, boolean timely) {
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

	public static void newScope(Runnable action) {
		newScope((conn) -> {
			action.run();
		});
	}

	/**
	 * 新建一个范围数据上下文，该数据上下文会创建一个新的独立事务
	 * 
	 * @param action
	 */
	public static void newScope(Consumer<DataAccess> action) {
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
}
