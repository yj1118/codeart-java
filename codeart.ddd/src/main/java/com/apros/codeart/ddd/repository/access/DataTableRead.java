package com.apros.codeart.ddd.repository.access;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.apros.codeart.ddd.ConstructorParameterInfo;
import com.apros.codeart.ddd.ConstructorRepositoryImpl;
import com.apros.codeart.ddd.DomainCollection;
import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.Emptyable;
import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.IDomainCollection;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.IValueObject;
import com.apros.codeart.ddd.MapData;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.ddd.repository.DataContext;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.Activator;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.ListUtil;
import com.google.common.collect.Iterables;

final class DataTableRead {
	private DataTable _self;

	public DataTableRead(DataTable self) {
		_self = self;
	}

	// <summary>
	/// 1对多的引用关系的读取
	/// </summary>
	/// <param name="rootId"></param>
	/// <param name="id"></param>
	/// <returns></returns>
	private Object readOneToMore(PropertyMeta tip, ConstructorParameterInfo prmTip, DomainObject parent, Object rootId,
			Object masterId, QueryLevel level) {
		var datas = queryRootAndSlaveIds(rootId, masterId);

		Class<?> implementType = null;
		if (parent == null) {
			// 说明还在构造阶段,或者是内部调用
			if (prmTip != null && prmTip.implementType() != null)
				implementType = prmTip.implementType();
			else
				implementType = _self.middle().objectType(); // middle表对应的是属性的基类类型
		} else {
			implementType = _self.middle().objectType(); // middle表对应的是属性的基类类型
		}
		var list = createList(parent, implementType, tip);
		var elementType = _self.middle().elementType();

		if (_self.type() == DataTableType.AggregateRoot) {
			// 引用了多个外部内聚根
			var model = DataModelLoader.get((Class<? extends IAggregateRoot>) elementType);
			var root = model.root();
			var slaveIdName = GeneratedField.SlaveIdName;

			var queryLevel = getQueryAggreateRootLevel(level);
			for (var data : datas) {
				var slaveId = data.get(slaveIdName);
				var item = (IDomainObject) root.querySingle(slaveId, queryLevel);
				if (!item.isEmpty()) {
					list.add(item);
				}
			}
		} else {
			var slaveIdName = GeneratedField.SlaveIdName;
			for (var data : datas) {
				var slaveId = data.get(slaveIdName);
				var item = (IDomainObject) _self.querySingle(rootId, slaveId);
				if (!item.isEmpty()) {
					list.add(item);
				}
			}
		}
		return list;
	}

	/// <summary>
	/// 读取基础数据的集合值
	/// </summary>
	/// <param name="tip"></param>
	/// <param name="prmTip"></param>
	/// <param name="parent"></param>
	/// <param name="rootId"></param>
	/// <param name="masterId"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	Object readValues(PropertyMeta tip, ConstructorParameterInfo prmTip, DomainObject parent, Object rootId,
			Object masterId, QueryLevel level) {
		var datas = queryPrimitiveValues(rootId, masterId);

		Class<?> implementType = null;
		if (parent == null) {
			// 说明还在构造阶段,或者是内部调用
			if (prmTip != null && prmTip.implementType() != null) {
				implementType = prmTip.implementType();
			} else
				implementType = _self.objectType();
		} else {
			implementType = _self.objectType();
		}
		var list = createList(parent, implementType, tip);
		var elementType = _self.elementType();

		var valueName = GeneratedField.PrimitiveValueName;
		for (var data : datas) {
			var value = data.get(valueName);
			list.add(value);
		}
		return list;
	}

	/// <summary>
	///
	/// </summary>
	/// <param name="parent"></param>
	/// <param name="listType"></param>
	/// <param name="property">集合在对象中的属性定义</param>
	/// <returns></returns>
	private Collection createList(DomainObject parent, Class<?> listType, PropertyMeta tip) {
		if (_isDomainCollection.apply(listType)) {
			var constructor = _getDomainCollectionConstructor.apply(listType);

			var collection = (IDomainCollection) constructor.newInstance(tip.monotype(),
					DomainProperty.getProperty(tip));
			collection.setParent(parent);
			return (Collection) collection;
		}
		return (Collection) Activator.createInstance(listType);
	}

	private static Function<Class<?>, Boolean> _isDomainCollection = LazyIndexer.init((type) -> {
		return type.isAssignableFrom(DomainCollection.class);
	});

	private static Function<Class<?>, Constructor> _getDomainCollectionConstructor = LazyIndexer.init((type) -> {
		return type.getConstructor(Class.class, DomainProperty.class);
	});

	/// <summary>
	/// 创建对象
	/// </summary>
	/// <param name="objectType"></param>
	/// <param name="data"></param>
	/// <returns></returns>
	private DomainObject createObject(Class<?> objectType, MapData data, QueryLevel level) {
		DomainObject obj = null;

		DataContext.using(() -> {
			if (data.isEmpty())
				obj = (DomainObject) DomainObject.getEmpty(objectType);
			else {
				obj = createObjectImpl(objectType, objectType, data, level);
			}
		});
		return obj;
	}

	/// <summary>
	///
	/// </summary>
	/// <param
	/// name="defineType">如果是动态领域对象，那么该类型为定义领域属性的类型（也就是定义类型），否则是对象的实际类型</param>
	/// <param name="objectType">实际存在内存中的实际类型</param>
	/// <param name="data"></param>
	/// <returns></returns>
	private DomainObject createObjectImpl(Class<?> defineType, Class<?> objectType, MapData data, QueryLevel level) {
		// 构造对象
		DomainObject obj = constructObject(objectType, data, level);

		// 设置代理对象
		setDataProxy(obj, data, level == QueryLevel.Mirroring);

		// 为了避免死循环，我们先将对象加入到构造上下文中
		addToConstructContext(obj, data);

		// 加载属性
		loadProperties(defineType, data, obj, level);

		removeFromConstructContext(obj);

		// 补充信息
		supplement(obj, data, level);

		return obj;
	}

	private void addToConstructContext(DomainObject obj, MapData data) {
		Object id = data.get(EntityObject.IdPropertyName);
		if (_self.type() == DataTableType.AggregateRoot) {
			ConstructContext.add(id, obj);
		} else {
			Object rootId = data.get(GeneratedField.RootIdName);
			ConstructContext.add(rootId, id, obj);
		}
	}

	private void removeFromConstructContext(DomainObject obj) {
		ConstructContext.remove(obj);
	}

	private DomainObject constructObject(Class<?> objectType, MapData data, QueryLevel level) {
		var constructorTip = ConstructorRepositoryImpl.getTip(objectType);
		var constructor = constructorTip.constructor();
		var args = createArguments(constructorTip, data, level);
		return (DomainObject) constructor.newInstance(args);
	}

	/// <summary>
	/// 加载属性
	/// </summary>
	/// <param name="objectType"></param>
	/// <param name="data"></param>
	/// <param name="obj"></param>
	private void loadProperties(Class<?> objectType, MapData data, DomainObject obj, QueryLevel level) {
		var propertyTips = PropertyMeta.getProperties(objectType); // 此处不必考虑是否为派生类，直接赋值所有属性
		for (var propertyTip : propertyTips) {
			// 只有是可以公开设置的属性和不是延迟加载的属性我们才会主动赋值
			// 有些属性是私有设置的，这种属性有可能是仅获取外部的数据而不需要赋值的
			// 如果做了inner处理，那么立即加载
			if ((propertyTip.isPublicSet() && !propertyTip.lazy()) || containsObjectData(propertyTip, data)) {
				var value = readPropertyValue(obj, propertyTip, null, data, level); // 已不是构造，所以不需要prmTip参数
				if (value == null) {
					throw new IllegalArgumentException(Language.strings("codeart.ddd", "LoadPropertyError",
							String.format("%s.%s", propertyTip.declaringType().getName(), propertyTip.name())));
				}

				obj.setValue(propertyTip.getProperty(), value);
			}
		}
	}

	private void setDataProxy(DomainObject obj, MapData data, boolean isMirror) {
		// 设置代理对象
		obj.dataProxy(new DataProxyImpl(data, _self, isMirror));
	}

	private void supplement(DomainObject obj, MapData data, QueryLevel level) {
		var valueObject = TypeUtil.as(obj, IValueObject.class);
		if (valueObject != null) {
			Object id = data.get(EntityObject.IdPropertyName);
			if (id != null) {
				valueObject.setPersistentIdentity((UUID) id);
			}
		}

		obj.markClean(); // 对象从数据库中读取，是干净的
	}

	private Object[] createArguments(ConstructorRepositoryImpl tip, MapData data, QueryLevel level) {
		var length = Iterables.size(tip.parameters());

		if (length == 0)
			return ListUtil.emptyObjects();
		Object[] args = new Object[length];
		var prms = tip.parameters();
		var prmsLength = prms.size();
		for (var i = 0; i < prmsLength; i++) {
			var prm = prms.get(i);
			var arg = createArgument(prm, data, level);
			args[i] = arg;
		}
		return args;
	}

	private Object createArgument(ConstructorParameterInfo prm, MapData data, QueryLevel level) {
		// 看构造特性中是否定义了加载方法
		var value = prm.loadData(_self.objectType(), data, level);
		if (value != null) {
			return value;
		}
		var tip = prm.propertyTip();
		if (tip == null)
			throw new IllegalStateException(Language.stringsMessageFormat("codeart.ddd",
					"ConstructionParameterNoProperty", _self.objectType().getName(), prm.name()));

		// 从属性定义中加载
		value = readPropertyValue(null, tip, prm, data, level); // 在构造时，还没有产生对象，所以parent为 null
		if (value == null)
			throw new IllegalStateException(Language.stringsMessageFormat("codeart.ddd", "ConstructionParameterError",
					prm.declaringType().getName(), prm.name()));
		return value;
	}

	/// <summary>
	///
	/// </summary>
	/// <param name="parent"></param>
	/// <param name="tip"></param>
	/// <param name="prmTip"></param>
	/// <param name="data"></param>
	/// <param name="level">对象在被加载时用到的查询级别</param>
	/// <returns></returns>
	public Object readPropertyValue(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prm, MapData data,
			QueryLevel level) {
		// 看对应的属性特性中是否定义了加载方法，优先执行自定义方法
		Object value = prm.loadData(_self.objectType(), data, level);
		if (value != null) {
			return value;
		}

		// 自动加载
		switch (tip.category()) {
		case DomainPropertyCategory.Primitive: {
			return readPrimitive(tip, data);
		}
		case DomainPropertyCategory.PrimitiveList: {
			return readPrimitiveList(parent, tip, prmTip, data, level);
		}
		case DomainPropertyCategory.AggregateRoot: {
			return readAggregateRoot(tip, data, level);
		}
		case DomainPropertyCategory.ValueObject:
		case DomainPropertyCategory.EntityObject: {
			return readMember(tip, data);
		}
		case DomainPropertyCategory.EntityObjectList:
		case DomainPropertyCategory.ValueObjectList:
		case DomainPropertyCategory.AggregateRootList: {
			return readMembers(parent, tip, prmTip, data, level);
		}
		}
		return null;
	}

//	#region 读取基础的值数据

	private Object readPrimitive(PropertyMeta tip, MapData data) {
		var value = tip.lazy() ? readValueByLazy(tip, data) : readValueFromData(tip, data);
		if (!tip.isEmptyable())
			return value;
		if (value == null) {
			// Emptyable类型的数据有可能存的是null值
			return Emptyable.createEmpty(tip.monotype());
		}
		return Emptyable.create(tip.monotype(), value);
	}

	private Object readValueFromData(PropertyMeta tip, MapData data) {
		return data.get(tip.name());
	}

	private Object readValueByLazy(PropertyMeta tip, MapData data) {
		Object id = data.get(EntityObject.IdPropertyName);
		if (id != null) {

			var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
					: GeneratedField.RootIdName;
			Object rootId = data.get(rootIdName);
			if (rootId != null) {
				return queryDataScalar(rootId, id, tip.name());
			}
		}
		return null;
	}

//	region 读取基础值的集合数据

	private Object readPrimitiveList(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prmTip,
			MapData data, QueryLevel level) {
		var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
				: GeneratedField.RootIdName;

		Object rootId = data.get(rootIdName);

		if (rootId != null) {
			// 当前对象的编号，就是子对象的masterId
			Object masterId = data.get(EntityObject.IdPropertyName);

			var child = _self.findChild(_self, tip);
			return child.readValues(tip, prmTip, parent, rootId, masterId, level);
		}
		return null;
	}

//	#endregion

	private static QueryLevel getQueryAggreateRootLevel(QueryLevel masterLevel) {
		// 除了镜像外，通过属性读取外部根，我们都是无锁的查询方式
		return masterLevel == QueryLevel.Mirroring ? QueryLevel.Mirroring : QueryLevel.None;
	}

	/// <summary>
	/// 获得子对象的数据
	/// </summary>
	/// <param name="name"></param>
	/// <param name="value"></param>
	/// <returns></returns>
	private MapData getObjectData(MapData data, PropertyMeta tip) {
		// 以前缀来最大化收集，因为会出现类似Good_Unit_Name 这种字段，不是默认字段，但是也要收集，是通过inner good.unit的语法来的
		var prefix = String.format("%s_", tip.name());

		MapData value = null;
		for (var p : data) {
			if (p.getKey().startsWith(prefix)) {
				if (value == null)
					value = new MapData();

				var name = p.getKey().substring(prefix.length());

				value.put(name, p.getValue());
			}
		}

		return value;
	}

	private boolean containsObjectData(PropertyMeta tip, MapData data) {
		DataTable table = null;

		switch (tip.category()) {
		case DomainPropertyCategory.AggregateRoot: {
			var model = DataModel.Create(tip.PropertyType);
			table = model.Root;
			break;
		}
		case DomainPropertyCategory.EntityObject:
		case DomainPropertyCategory.ValueObject: {
			table = _self.findChild(_self, tip);
			break;
		}
		default:
			return false;
		}

		// 以默认字段来验证
		var fields = table.defaultQueryFields();

		for (var field : fields) {
			var name = String.format("%s %ss", tip.name(), field.name());
			if (!data.containsKey(name))
				return false;
		}
		return true;
	}

	private Object readAggregateRoot(PropertyMeta tip, MapData data, QueryLevel level)
    {
        var model = DataModel.create(tip.monotype());

        if (TryGetObjectData(data, tip, out var item))
        {
            DynamicData entry = item as DynamicData;
            var obj = model.Root.CreateObject(tip.PropertyType, entry, QueryLevel.None); //从数据中直接加载的根对象信息，一定是不带锁的

            //数据填充的对象，不加载镜像（为了提高性能）
            //if (((IDomainObject)obj).IsEmpty() && model.ObjectTip.Snapshot)
            //{
            //    //加载快照
            //    obj = model.Snapshot.QuerySingle(id, QueryLevel.None);
            //}

            return obj;
        }


        var dataKey = _getIdName(tip.PropertyName);

        object id = null;
        if (data.TryGetValue(dataKey, out id))
        {
            var queryLevel = getQueryAggreateRootLevel(level);
            var obj = model.Root.QuerySingle(id, queryLevel);

            if (((IDomainObject)obj).IsEmpty() && model.ObjectTip.Snapshot)
            {
                //加载快照
                obj = model.Snapshot.QuerySingle(id, QueryLevel.None);
            }

            return obj;
        }
        return null;
    }

	private Object readMember(PropertyMeta tip, MapData data) {
		return tip.lazy() ? readMemberByLazy(tip, data) : readMemberFromData(tip, data);
	}

	private Object ReadMemberFromData(PropertyRepositoryAttribute tip, DynamicData data)
    {
        var name = _getNameWithSeparated(tip.PropertyName);
        var subData = new DynamicData(); //由于subData要参与构造，所以不从池中取
        foreach (var p in data)
        {
            var dataName = p.Key;
            if (dataName.StartsWith(name))
            {
                var subName = _getNextName(dataName);
                subData.Add(subName, p.Value);
            }
        }

        if (subData.IsEmpty())
        {
            if (tip.DomainPropertyType == DomainPropertyType.AggregateRoot)
            {
                var idName = _getIdName(tip.PropertyName);
                var id = data.Get(idName);
                return ReadSnapshot(tip, id);
            }
            return DomainObject.GetEmpty(tip.PropertyType);
        }


        var typeKey = (string)subData.Get(GeneratedField.TypeKeyName);
        Type objectType = null;
        if (this.IsDynamic)
        {
            objectType = tip.PropertyType;
        }
        else
        {
            objectType = string.IsNullOrEmpty(typeKey) ? tip.PropertyType : DerivedClassAttribute.GetDerivedType(typeKey);
        }


        var child = GetRuntimeTable(this, tip.PropertyName, objectType);
        //先尝试中构造上下文中得到
        return child.GetObjectFromConstruct(subData) ?? child.CreateObject(objectType, subData, QueryLevel.None); //成员始终是QueryLevel.None的方式加载
    }

	private object ReadMemberByLazy(PropertyRepositoryAttribute tip, DynamicData data)
    {
        var child = GetChildTableByRuntime(this, tip);
        var dataKey = _getIdName(tip.PropertyName);

        object id = null;
        if (data.TryGetValue(dataKey, out id))
        {
            object rootId = null;
            var rootIdName = this.Type == DataTableType.AggregateRoot
                                            ? EntityObject.IdPropertyName
                                            : GeneratedField.RootIdName;
            if (data.TryGetValue(rootIdName, out rootId))
            {
                var member = child.QuerySingle(rootId, id);
                if(tip.DomainPropertyType == DomainPropertyType.AggregateRoot)
                {
                    //尝试加载快照
                    if (member.IsNull())
                        return ReadSnapshot(tip, id);
                }
                return member;
            }
        }
        return null;
    }

	Object readMembers(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prmTip, MapData data,
			QueryLevel level) {

		var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
				: GeneratedField.RootIdName;

		var rootId = data.get(rootIdName);

		if (rootId != null) {
			// 当前对象的编号，就是子对象的masterId
			object masterId = data.get(EntityObject.IdPropertyName);

			var child = _self.findChild(_self, tip);
			return child.readOneToMore(tip, prmTip, parent, rootId, masterId, level);
		}
		return null;
	}

	/**
	 * 读取对象集合的内部调用版本，此方法不是用于构造对象，而是为了查询用
	 * 
	 * @param tip
	 * @param data
	 * @param objs
	 */
	public void queryMembers(PropertyMeta tip, MapData data, ArrayList<Object> objs) {

		var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
				: GeneratedField.RootIdName;

		Object rootId = data.get(rootIdName);

		// 当前对象的编号，就是子对象的masterId
		Object masterId = data.get(EntityObject.IdPropertyName);

		var child = _self.findChild(_self, tip);
		child.queryOneToMore(rootId, masterId, objs);
	}

	public void queryOneToMore(Object rootId, Object masterId, ArrayList<Object> objs) {
		var datas = queryRootAndSlaveIds(rootId, masterId);

		var slaveIdName = GeneratedField.SlaveIdName;
		for (var data : datas) {
			var slaveId = data.get(slaveIdName);
			var item = _self.querySingle(rootId, slaveId);
			objs.add(item);
		}
	}

	public Object queryMember(PropertyMeta tip, MapData data) {
		var child = _self.findChild(_self, tip); // 通过基本表就可以实际查出数据，查数据会自动识别数据的真实类型的

		var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
				: GeneratedField.RootIdName;

		var rootId = data.get(rootIdName);

		if (rootId != null) {

			var field = DataTableUtil.getQuoteField(_self, tip.name());

			Object id = data.get(field.name());

			if (id != null) {
				return child.querySingle(rootId, id);
			}
		}

		return null;
	}

//	#region 查询数据

	/// <summary>
	/// 查询单值数据，不必缓存，因此延迟就在后就被加载到内存中已被缓存的对象了
	/// </summary>
	/// <param name="rootId"></param>
	/// <param name="id"></param>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	private Object queryDataScalar(Object rootId, Object id, String propertyName)
    {
        var query = GetScalarByIdExpression(this, propertyName);
        using (var temp = SqlHelper.BorrowData())
        {
            var param = temp.Item;
            if (this.Type != DataTableType.AggregateRoot)
            {
                param.Add(GeneratedField.RootIdName, rootId);
            }
            param.Add(EntityObject.IdPropertyName, id);

            var sql = query.Build(param, this);
            return SqlHelper.ExecuteScalar(sql, param);
        }
    }

	/**
	 * 
	 * 查询1对多引用的成员数据
	 * 
	 * @param rootId
	 * @param masterId
	 * @param datas
	 */
	private Iterable<MapData> queryRootAndSlaveIds(Object rootId, Object masterId) {
		// 查询涉及到中间表,对对象本身没有任何条件可言
		var qb = DataSource.getQueryBuilder(GetSlaveIdsQB.class);
		var sql = qb.build(new QueryDescription(_self));

		var param = new MapData();
		param.put(GeneratedField.RootIdName, rootId);
		// if (!this.Root.IsEqualsOrDerivedOrInherited(this.Master))
		if (!_self.root().same(_self.master())) {
			param.put(GeneratedField.MasterIdName, masterId);
		}

		return DataAccess.getCurrent().queryRows(sql, param);
	}

	/**
	 * 
	 * 查询基础值的集合的数据
	 * 
	 * 
	 * @param rootId
	 * @param masterId
	 * @return
	 */
	private Iterable<MapData> queryPrimitiveValues(Object rootId, Object masterId) {
		// 查询涉及到中间表,对对象本身没有任何条件可言
		var qb = DataSource.getQueryBuilder(GetPrimitiveValuesQB.class);
		var sql = qb.build(new QueryDescription(_self));

		var param = new MapData();
		param.put(GeneratedField.RootIdName, rootId);
		if (!_self.root().same(_self.master())) {
			param.put(GeneratedField.MasterIdName, masterId);
		}

		return DataAccess.getCurrent().queryRows(sql, param);
	}

//	#endregion

	private static QueryExpression GetScalarByIdExpression(DataTable table, string propertyName)
    {
        return _getScalarById(table)(propertyName);
    }

	private static Func<DataTable, Func<string, QueryExpression>> _getScalarById = LazyIndexer.Init<DataTable, Func<string,QueryExpression>>((table)=>
	{
        return LazyIndexer.Init<string, QueryExpression>((propertyName) =>
        {
            string expression = null;

            if (table.Type == DataTableType.AggregateRoot)
            {
                expression = string.Format("[{0}=@{0}][select {1}]", EntityObject.IdPropertyName, propertyName);
            }
            else
            {
                expression = string.Format("[{0}=@{0} and {1}=@{1}][select {2}]", GeneratedField.RootIdName, EntityObject.IdPropertyName, propertyName);
            }
            return QueryObject.Create(table, expression, QueryLevel.None);
        });
    });

}
