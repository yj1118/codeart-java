package apros.codeart.ddd;

import static apros.codeart.i18n.Language.strings;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import apros.codeart.ddd.internal.DTOMapper;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.dto.serialization.IDTOSerializable;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.EventHandler;
import apros.codeart.util.INullProxy;
import apros.codeart.util.LazyIndexer;

/*
 * 
 * 对于领域对象属性是否改变的约定：
1.如果属性为普通类型（int、string等基础类型）,根据值是否发生了改变来判定属性是否改变
2.如果属性为值对象类型（ValueObject）,根据ValueObject的值是否发生了改变来判定属性是否改变
3.如果属性为实体对象类型（EntityObject,EntityObjectPro,Aggregate）,只要赋值，属性就会发生改变；
   实体对象内部的属性发生改变，不影响外部属性的变化，因为他们的引用关系并没有被改变；
 4.如果属性为实体对象类型的集合（ObjectCollection(EntityObject),ObjectCollection(EntityObjectPro),ObjectCollection(Aggregate)）,只要赋值，属性就会发生改变；
   单个实体对象内部的属性发生改变，不影响外部属性的变化，因为他们的引用关系并没有被改变；
   实体对象集合的成员（数量或者成员被替换了）发生了变化，那么属性被改变，因为集合代表的是引用对象和多个被引用对象之间的关系，集合成员变化了，代表引用关系也变化了
 * 
 * 要截获属性的set方法，可以自行定义setXXX方法，在方法内部调用xxx(value)来更新属性，并扩展逻辑
 * 新版中不在支持ProerptySet标签，因为用途不大，且写法并不够优美，还会代码烦人的调用链跟踪
 * 
 * 
 * 
 */
public abstract class DomainObject implements IDomainObject, INullProxy, IDTOSerializable {

	private ObjectMeta _meta;

	/**
	 * 
	 * 领域对象的类型，请注意，在动态领域对象下，该类型是对应的领域对象的类型，而不是DynamicRoot、DynamicValueObject等载体对象
	 * 
	 * @return
	 */
	public ObjectMeta meta() {
		return _meta;
	}

	/**
	 * 领域对象的继承深度（注意，仅从DomainObject基类开始算起）
	 */
	private int _typeDepth;

	public DomainObject() {
		_meta = ObjectMetaLoader.get(this.getClass());
		_constructedDepth = 0;
		_typeDepth = getTypeDepth();
		this.onConstructed();
	}

	private int getTypeDepth() {
		var depth = TypeUtil.getDepth(this.getClass());

		var baseType = DomainObject.class.getSuperclass();
		if (baseType == null)
			return depth;
		return depth - TypeUtil.getDepth(baseType);
	}

	/**
	 * 
	 * 对象是否为一个快照，如果该值为true，表示对象已经被仓储删除或者其属性值已被修改
	 * 
	 * @return
	 */
	public boolean isSnapshot() {
		return this.dataProxy().isSnapshot(); // 通过数据代理我们得知数据是否为快照
	}

	/**
	 * 
	 * 对象是否为一个镜像
	 * 
	 * @return
	 */
	public boolean isMirror() {
		return this.dataProxy().isMirror(); // 通过数据代理我们得知数据是否为快照
	}

	/**
	 * 对象是否为无效的，对象无效的原因可能有很多（比如：是个快照对象，或者其成员属性是快照，导致自身无效化了）
	 * 默认情况下，快照对象就是一个无效对象，可以通过重写该属性的算法，针对某些业务判定对象是无效的 无效的对象只能被删除，不能新增和修改
	 * 
	 * @return
	 */
	public boolean invalid() {
		return this.isSnapshot();
	}

	public boolean isEmpty() {
		// 默认情况下领域对象是非空的
		return false;
	}

	public boolean isNull() {
		return this.isEmpty();
	}

	/**
	 * 是否追踪属性变化的情况，该值为true，会记录属性更改前的值，这会消耗一部分内存
	 * 
	 * @return
	 */
	public boolean isTrackPropertyChange() {
		return !this.propertyChanged.isEmpty();
		// 这里要看属性是否绑定了改变事件，稍后补充
//		return ObjectLogableAttribute.GetTip(this.ObjectType) != null;
	}

//
//	#region 状态

	private StateMachine _machine = new StateMachine();

	/// <summary>
	/// 是否为脏对象
	/// </summary>
	public boolean isDirty() {
		return _machine.isDirty() || hasDirtyProperty();
	}

	/// <summary>
	/// 内存中新创建的对象
	/// </summary>
	public boolean isNew() {
		return _machine.isNew();
	}

	/**
	 * 该对象是否被改变
	 * 
	 * @return
	 */
	public boolean isChanged() {
		return _machine.isChanged();
	}

	public void markDirty() {
		_machine.markDirty();
	}

	public void markNew() {
		_machine.markNew();
	}

	/// <summary>
	/// 设置对象为干净的
	/// </summary>
	public void markClean() {
		_machine.markClean();
		// 当对象为干净对象时，那么对象的所有内聚成员应该也是干净的
		markCleanProperties();
	}

	private void markCleanProperties() {
		invokeProperties((obj) -> {
			obj.markClean();
		});
	}

	private StateMachine _backupMachine;

	public void saveState() {
		saveStateProperties();
		if (_backupMachine == null) {
			_backupMachine = _machine.clone();
		} else {
			_backupMachine.combine(_machine);
		}
	}

	private void saveStateProperties() {
		invokeProperties((obj) -> {
			obj.saveState();
		});
	}

	/// <summary>
	/// 加载备份的状态，此时备份会被删除
	/// </summary>
	public void loadState() {
		loadStateProperties();
		if (_backupMachine != null) {
			_machine = _backupMachine;
			_backupMachine = null; // 加载状态后，将备份给删除
		}
	}

	private void loadStateProperties() {
		invokeProperties((obj) -> {
			obj.loadState();
		});
	}

	/// <summary>
	/// 当已存在状态备份时，如果有属性发生改变，那么需要同步状态的备份，这样才不会造成BUG
	/// </summary>
	/// <param name="propertyName"></param>
	private void duplicateMachineSetPropertyChanged(String propertyName) {
		if (_backupMachine != null)
			_backupMachine.setPropertyChanged(propertyName);
	}

	private void duplicateMachineClearPropertyChanged(String propertyName) {
		if (_backupMachine != null)
			_backupMachine.clearPropertyChanged(propertyName);
	}

//	/**
//	 * 
//	 * 根据对比结果，设置属性是否被更改
//	 * 
//	 * @param <T>
//	 * @param property
//	 * @param oldValue
//	 * @param newValue
//	 * @return
//	 */
//	private <T> boolean setPropertyChanged(DomainProperty property, T oldValue, T newValue) {
//		// 要用Equals判断
//		if (Objects.equal(oldValue, newValue))
//			return false;
//		_machine.setPropertyChanged(property.name());
//		duplicateMachineSetPropertyChanged(property.name());
//		return true;
//	}

	/**
	 * 强制设置属性已被更改
	 * 
	 * @param property
	 */
	void setPropertyChanged(DomainProperty property) {
		_machine.setPropertyChanged(property.name());
		duplicateMachineSetPropertyChanged(property.name());
	}

	/**
	 * 清除属性被改变的状态
	 * 
	 * @param property
	 */
	protected void clearPropertyChanged(DomainProperty property) {
		_machine.clearPropertyChanged(property.name());
		duplicateMachineClearPropertyChanged(property.name());
	}

	/**
	 * 
	 * 判断属性是否被更改，对于领域对象属性是否改变的约定：
	 * 
	 * 1.如果属性为普通类型（int、string等基础类型）,根据值是否发生了改变来判定属性是否改变
	 * 2.如果属性为值对象类型（ValueObject）,根据ValueObject的值是否发生了改变来判定属性是否改变
	 * 3.如果属性为实体对象类型（EntityObject,Aggregate）,只要赋值，属性就会发生改变；
	 * 实体对象内部的属性发生改变，虽然引用关系没有变，但是我们认为属性还是发生变化了；
	 * 4.如果属性为实体对象类型的集合（ObjectCollection(EntityObject),ObjectCollection(EntityObjectPro),ObjectCollection(Aggregate)）,只要赋值，属性就会发生改变；
	 * 单个实体对象内部的属性发生改变，不影响外部属性的变化，因为他们的引用关系并没有被改变；
	 * 实体对象集合的成员发生了变化，那么属性被改变，因为集合代表的是引用对象和多个被对象之间的关系，集合成员变化了，代表引用关系也变化了
	 * 
	 * @param property
	 * @return
	 */
	public boolean isPropertyChanged(DomainProperty property) {
		return isPropertyChanged(property.name());
	}

	/**
	 * 仅仅只是属性 {@code property} 发生了改变
	 * 
	 * @param property
	 * @return
	 */
	public boolean onlyPropertyChanged(DomainProperty property) {
		return _machine.onlyPropertyChanged(property.name());
	}

	/**
	 * 
	 * 判断属性是否被更改
	 * 
	 * @param propertyName
	 * @return
	 */
	public boolean isPropertyChanged(String propertyName) {
		return _machine.isPropertyChanged(propertyName);
	}

	/**
	 * 
	 * 属性是否为脏的
	 * 
	 * @param property
	 * @return
	 */
	public boolean isPropertyDirty(DomainProperty property) {
		if (this.isNew())
			return true; // 如果是新建对象，那么属性一定是脏的
		var isChanged = isPropertyChanged(property);
		if (isChanged)
			return true; // 属性如果被改变，那么我们认为就是脏的，这是一种粗略的判断，因为就算属性改变了，有可能经过2次修改后变成与数据库一样的值，这样就不是脏的了，但是为了简单化处理，我们仅认为只要改变了就是脏的，这种判断不过可以满足100%应用
		// 如果属性没有被改变，那么我们需要进一步判断属性的成员是否发生改变
		switch (property.category()) {
		case DomainPropertyCategory.ValueObject:
		case DomainPropertyCategory.EntityObject: {
			DomainObject obj = tryGetValue(property);
			if (obj != null) {
				// 如果加载了，就进一步判断
				return obj.isDirty();
			}
		}
			break;
		case DomainPropertyCategory.ValueObjectList:
		case DomainPropertyCategory.EntityObjectList: {
			Iterable<DomainObject> list = tryGetValue(property);
			if (list != null) {
				// 如果加载了，就进一步判断
				for (DomainObject obj : list) {
					if (obj.isDirty())
						return true;
				}
			}
		}
			break;
		default: {

		}
		}
		// AggregateRoot和AggregateRootList
		// 只用根据isPropertyChanged判断即可，因为他们是外部内聚根对象，是否变脏与本地内聚根没有关系
		return false;
	}

	public boolean isPropertyDirty(String propertyName) {
		var property = DomainProperty.getProperty(this.getClass(), propertyName);
		return isPropertyDirty(property);
	}

	/**
	 * 
	 * 是否有脏的属性
	 * 
	 * @return
	 */
	private boolean hasDirtyProperty() {
		var properties = DomainProperty.getProperties(this.getClass());
		for (var property : properties) {
			if (isPropertyDirty(property.name()))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * 在有效的属性对象上执行方法，只有被加载了的对象才执行
	 * 
	 * @param action
	 */
	private void invokeProperties(Consumer<DomainObject> action) {
		var properties = DomainProperty.getProperties(this.getClass());
		for (var property : properties) {
			switch (property.category()) {
			case DomainPropertyCategory.EntityObject:
			case DomainPropertyCategory.ValueObject: {
				DomainObject obj = tryGetValue(property);
				if (obj != null) {
					action.accept(obj);
				}
			}
				break;
			case DomainPropertyCategory.EntityObjectList:
			case DomainPropertyCategory.ValueObjectList: {
				Iterable<DomainObject> list = tryGetValue(property);
				if (list != null) {
					for (DomainObject obj : list) {
						action.accept(obj);
					}
				}
			}
				break;
			default:
				break;
			}
		}
	}

	private IDataProxy _dataProxy;

	public IDataProxy dataProxy() {
		if (_dataProxy == null) {
			_dataProxy = DataProxy.createStorage(this);
			// 以下是老版本代码，新版本中加载领域对象是各个会话加载各自的对象，不存在冲突，所以不用锁
			// 但是为了保险起见，先保留老代码，以便参考
//            lock(_syncObject)
//            {
//                if (_dataProxy == null)
//                {
//                    _dataProxy = CodeArt.DomainDriven.DataProxy.CreateStorage(this);
//                }
//            }
		}
		return _dataProxy;
	}

	public void dataProxy(IDataProxy value) {
		if (_dataProxy != null && value != null)
			value.copy(_dataProxy);
		_dataProxy = value;
		if (_dataProxy != null)
			_dataProxy.setOwner(this);
	}

	public boolean isPropertyLoaded(DomainProperty property) {
		return this.dataProxy().isLoaded(property.name());
	}

	public boolean isPropertyLoaded(String propertyName) {
		var property = DomainProperty.getProperty(this.getClass(), propertyName);
		return this.dataProxy().isLoaded(property.name());
	}

	public int dataVersion() {
		return this.dataProxy().getVersion();
	}

	/**
	 * 同步数据版本号，当确认对象是干净的情况下，你可以手动更新版本号，这在某些情况下很有用
	 */
	public void syncDataVersion() {
		this.dataProxy().syncVersion();
	}

//	region 固定规则

	/// <summary>
	/// 固定规则
	/// </summary>
	public IFixedRules fixedRules() {
		return FixedRules.Instance;
	}

	/**
	 * 验证固定规则
	 */
	public ValidationResult validate() {
		return this.fixedRules().validate(this);
	}

	/**
	 * 对象验证器
	 */
	public Iterable<IObjectValidator> validators() {
		return _meta.validators();
	}

//	#
//
//	region 领域属性

	/**
	 * 
	 * 该方法将 {@value} 的值加载到属性 {@propertyName} 中，不会触发业务事件，仅用作装载数据用
	 * 
	 * @param propertyName
	 * @param value
	 */
	public void loadValue(String propertyName, Object value) {
		var oldValue = this.dataProxy().load(propertyName);
		this.dataProxy().save(propertyName, value, oldValue);
	}

	/**
	 * 
	 * 该方法虽然是公开的，但是 {@code property} 都是由类内部定义的，所以获取值和设置值只能通过常规的属性操作，无法通过该方法
	 * 
	 * @param property
	 * @param value
	 */
	public void setValue(DomainProperty property, Object value) {
		var oldValue = this.dataProxy().load(property.name());
		boolean isChanged = false;

		if (property.isChanged(oldValue, value)) {
			isChanged = true;
			this.setPropertyChanged(property);
		}

		// if (property.ChangedMode == PropertyChangedMode.Compare)
		// {
		// if (this.SetPropertyChanged(property, oldValue, value))
		// {
		// isChanged = true;
		// }
		// }
		// else if (property.ChangedMode == PropertyChangedMode.Definite)
		// {
		// this.SetPropertyChanged(property);
		// isChanged = true;
		// }

		if (isChanged) {
			if (property.isCollection()) {
				var collection = (IDomainCollection) value;
				collection.setParent(this);
			}

			this.dataProxy().save(property.name(), value, oldValue);

			handlePropertyChanged(property, value, oldValue);
		}
	}

	/**
	 * 
	 * 该方法虽然是公开的，但是 {@code property} 都是由类内部定义的，
	 * 
	 * 所以获取值和设置值只能通过常规的属性操作，无法通过该方法
	 * 
	 * @param <T>
	 * @param property
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(DomainProperty property, Class<T> valueType) {
		var value = getValue(property);
		if (value == null)
			Preconditions.checkNotNull(value, strings("codeart.ddd", "DomainCanNotNull", property.name()));
		return (T) value;
	}

	/**
	 * 
	 * 当属性的值已经被加载，就获取数据，否则不获取
	 * 
	 * @param property
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T tryGetValue(DomainProperty property) {
		if (this.dataProxy().isLoaded(property.name())) {
			return (T) getValue(property);
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param property
	 * @return
	 */
	public Object getValue(DomainProperty property) {
		return this.dataProxy().load(property.name());
	}

	public Object getValue(String propertyName) {
		return this.dataProxy().load(propertyName);
	}

	/**
	 * 
	 * 获得属性最后一次被更改前的值
	 * 
	 * @param property
	 * @return
	 */
	public Object getOldValue(DomainProperty property) {
		return this.dataProxy().loadOld(property.name());
	}

	/**
	 * 外界标记某个属性发生了变化，这常常是由于属性本身的成员发生了变化，而触发的属性变化 该方法会触发属性被改变的事件
	 * 
	 * @param property
	 */
	public void markPropertyChanged(DomainProperty property) {
		if (!this.isPropertyLoaded(property))
			return;

		this.setPropertyChanged(property);
		var value = this.getValue(property);
		handlePropertyChanged(property, value, value);
	}

	/**
	 * 
	 * 处理属性被改变时的行为
	 * 
	 * @param property
	 * @param newValue
	 * @param oldValue
	 */
	private void handlePropertyChanged(DomainProperty property, Object newValue, Object oldValue) {
		readOnlyCheckUp();
		raisePropertyChanged(property, newValue, oldValue);
		raiseChangedEvent();
	}

	private void raisePropertyChanged(DomainProperty property, Object newValue, Object oldValue) {
		if (this.isConstructing())
			return;// 构造时，不触发任何事件

		this.propertyChanged.raise(this, () -> {
			return new DomainPropertyChangedEventArgs(property, newValue, oldValue);
		});
	}

	public final EventHandler<DomainPropertyChangedEventArgs> propertyChanged = new EventHandler<DomainPropertyChangedEventArgs>();

	// 该方法似乎没什么用，所以不提供了，todo
//	/// <summary>
//	/// 得到被更改了的领域属性的信息
//	/// </summary>
//	public IEnumerable<(
//	DomainProperty Property, object newValue,
//	object oldValue)>
//
//	GetChangedProperties()
//    {
//        var properties = DomainProperty.GetProperties(this.ObjectType);
//        List<(DomainProperty Property, object newValue, object oldValue)> items = new List<(DomainProperty Property, object newValue, object oldValue)>();
//        foreach (var property in properties)
//        {
//            if (this.IsPropertyChanged(property))
//            {
//                var newValue = this.GetValue(property);
//                var oldValue = this.GetOldValue(property);
//                items.Add((property, newValue, oldValue));
//            }
//        }
//        return items;
//    }

//	#endregion
//
//	#
//
//	region 构造

	private EventHandler<DomainObjectConstructedEventArgs> _constructed;

	public EventHandler<DomainObjectConstructedEventArgs> constructed() {
		if (_constructed == null)
			_constructed = new EventHandler<DomainObjectConstructedEventArgs>();
		return _constructed;
	}

	private int _constructedDepth;

	public boolean isConstructing() {
		return _constructedDepth != _typeDepth;
	}

	/// <summary>
	/// 指示对象已完成构造，请务必在领域对象构造完成后调用此方法
	/// </summary>
	protected void onConstructed() {
		_constructedDepth++;

		if (_constructedDepth > _typeDepth) {
			throw new DomainDrivenException(Language.strings("codeart.ddd", "ConstructedError"));
		}

		if (!this.isConstructing()) // 已构造完毕
		{
			this.raiseConstructedEvent();
		}
	}

	private void raiseConstructedEvent() {
		if (this.isEmpty())
			return;
		if (_constructed != null) {
			_constructed.raise(this, () -> new DomainObjectConstructedEventArgs(this));
		}
		// 执行边界事件
		StatusEvent.execute(StatusEventType.Constructed, this);
	}

	private EventHandler<DomainObjectChangedEventArgs> _changed;

	public EventHandler<DomainObjectChangedEventArgs> changed() {
		if (_changed == null)
			_changed = new EventHandler<DomainObjectChangedEventArgs>();
		return _changed;
	}

	private void raiseChangedEvent() {
		if (this.isEmpty())
			return;// 空对象，不触发任何事件

		if (this.isConstructing())
			return;// 构造时，不触发任何事件

		onChanged();

		StatusEvent.execute(StatusEventType.Changed, this);
	}

	protected void onChanged() {
		if (_changed == null)
			return;

		this._changed.raise(this, () -> {
			return new DomainObjectChangedEventArgs(this);
		});
	}

	protected void readOnlyCheckUp() {
		if (this.isConstructing())
			return; // 构造阶段不处理

		if (this.isEmpty())
			throw new DomainDrivenException(
					Language.strings("codeart.ddd", "EmptyReadOnly", this.getClass().getName()));
	}

//	region 辅助方法

	/**
	 * 获取领域类型定义的空对象
	 * 
	 * @param objectType
	 * @return
	 */
	public static DomainObject getEmpty(Class<?> objectType) {
		return _getEmpty.apply(objectType);
	}

	private static Function<Class<?>, DomainObject> _getEmpty = LazyIndexer.init((objectType) -> {

		var empty = FieldUtil.getField(objectType, "Empty");
		if (empty == null)
			throw new DomainDrivenException(Language.strings("codeart.ddd", "NotFoundEmpty", objectType.getName()));

		var objEmpty = TypeUtil.as(empty, DomainObject.class);
		if (objEmpty == null)
			throw new DomainDrivenException(
					Language.strings("codeart.ddd", "TypeMismatchFor", empty.getClass(), DomainObject.class));
		return objEmpty;
	});

	/**
	 * 
	 * 判断类型是否表示一个空的领域对象
	 * 
	 * @param objectType
	 * @return
	 */
	public static boolean isEmpty(Class<?> objectType) {
		return objectType.getSimpleName().endsWith("Empty");// 为了不触发得到空对象带来的连锁构造，我们简单的认为空对象的名称末尾是 Empty即可，这也是CA的空对象定义约定
	}

	/**
	 * 从dto中加载数据
	 */
	public void load(DTObject data) {
		DTOMapper.load(this, data);
	}

	@Override
	public void serialize(DTObject owner, String name) {
		if (this.isEmpty())
			return;
		// 一次性使用this.getData(),所以可以用combineObject，性能高
		owner.combineObject(name, this.getData());
	}

	@Override
	public DTObject getData() {
		return DTOMapper.toDTO(this, (obj) -> {
			var meta = ObjectMetaLoader.get(obj.getClass());
			return meta.properties();
		}, (p) -> p.name());
	}

}
