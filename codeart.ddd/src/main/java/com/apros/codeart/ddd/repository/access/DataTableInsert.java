package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.Dictionary;
import com.apros.codeart.ddd.DomainBuffer;
import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.StringUtil;

final class DataTableInsert {

	private DataTable _self;

	public DataTableInsert(DataTable self) {
		_self = self;
	}

	public void exec(DomainObject obj) {
		if (obj == null || obj.isEmpty())
			return;
		if (!obj.isDirty()) {
			// 脏对象才有保存的必要
			return;
		}

		DomainObject root = null;
		if (_self.type() == DataTableType.AggregateRoot)
			root = obj;
		if (root == null || root.isEmpty())
			throw new IllegalStateException(
					Language.strings("codeart.ddd", "PersistentObjectError", obj.getClass().getName()));

		onPreDataInsert(obj);
		var data = insertData(root, null, obj);
		onDataInserted(root, obj, data);
	}

	/// <summary>
	/// 插入数据
	/// </summary>
	/// <param name="obj"></param>
	/// <returns></returns>
	private Dictionary insertData(DomainObject root, DomainObject parent, DomainObject obj) {
		var data = getInsertData(root, parent, obj);

		DataAccess.getCurrent().execute(this.sqlInsert(), data);

		return data;
	}

	private void onPreDataInsert(DomainObject obj) {
		_self.mapper().onPreInsert(obj, _self);
	}

	/**
	 * 
	 * 该方法用于保存数据后，更新基表的信息
	 * 
	 * @param root
	 * @param obj
	 * @param objData
	 */
	private void onDataInserted(DomainObject root, DomainObject obj, Dictionary objData) {
		if (_self.type() == DataTableType.AggregateRoot) {
			var ar = (IAggregateRoot) obj;
			DomainBuffer.add(ar);
		}

		_self.mapper().onInserted(obj, _self);
	}

	private Dictionary getInsertData(DomainObject root, DomainObject parent, DomainObject obj) {
		Class<?> objectType = _self.objectType();

		var tips = PropertyMeta.getProperties(objectType);
		var data = new Dictionary();
		for (var tip : tips) {
			insertAndCollectValue(root, parent, obj, tip, data);
		}

		switch (_self.type()) {
		case DataTableType.ValueObject: {
			// 需要补充编号
			data.put(EntityObject.IdPropertyName, DataTableUtil.getObjectId(obj));
			// 插入时默认为1
			data.put(GeneratedField.AssociatedCountName, 1);
			// 补充外键
			data.put(GeneratedField.RootIdName, DataTableUtil.getObjectId(root));
			break;
		}
		case DataTableType.EntityObject: {
			// 插入时默认为1
			data.put(GeneratedField.AssociatedCountName, 1);
			// 补充外键
			data.put(GeneratedField.RootIdName, DataTableUtil.getObjectId(root));
			break;
		}
		case DataTableType.Middle: {
			// 补充外键
			data.put(GeneratedField.RootIdName, DataTableUtil.getObjectId(root));
			break;
		}
		}

		// this.Mapper.FillInsertData(obj, data, this);

		// 只有非派生表才记录TypeKey和DataVersion
		data.put(GeneratedField.TypeKeyName, StringUtil.empty()); // 追加类型编号，非派生类默认类型编号为空
		data.put(GeneratedField.DataVersionName, 1); // 追加数据版本号

		return data;
	}

	/// <summary>
	/// 插入成员数据
	/// </summary>
	/// <param name="root"></param>
	/// <param name="parent"></param>
	/// <param name="obj"></param>
	/// <returns>成员有可能已经在别的引用中被插入，此时返回false,否则返回true</returns>
	private void insertMember(DomainObject root, DomainObject parent, DomainObject obj) {
		if (obj == null || obj.isEmpty())
			return;

		// 我们需要先查，看数据库中是否存在数据，如果不存在就新增，存在就增加引用次数
		var existObject = QuerySingle(GetObjectId(root), GetObjectId(obj));

		if (existObject.IsNull()) {
			OnPreDataInsert(obj);
			var data = InsertData(root, parent, obj);
			OnDataInserted(root, obj, data);
		} else {
			if (this.IsDerived) {
				this.InheritedRoot.IncrementAssociated(GetObjectId(root), GetObjectId(obj));
			} else {
				// 递增引用次数
				IncrementAssociated(GetObjectId(root), GetObjectId(obj));
			}
		}
	}

	private void InsertMiddle(IDomainObject root, IDomainObject master, IEnumerable slaves)
	{
	    if(this.IsPrimitiveValue)
	    {
	        InsertMiddleByValues(root, master, slaves);
	        return;
	    }

	    var rootId = GetObjectId(root);
	    var rootIdName = GeneratedField.RootIdName;
	    var slaveIdName = GeneratedField.SlaveIdName;

	    if (this.Root.IsEqualsOrDerivedOrInherited(this.Master))
	    {
	        int index = 0;
	        for (var slave : slaves)
	        {
	            if (slave.IsNull()) continue;
	            var slaveId = GetObjectId(slave);
	            var data = new Dictionary();
                data.put(rootIdName, rootId);
                data.put(slaveIdName, slaveId);
                data.put(GeneratedField.OrderIndexName, index);
                if (this.IsSessionEnabledMultiTenancy)
                    data.Add(GeneratedField.TenantIdName, AppSession.TenantId);
                SqlHelper.Execute(this.SqlInsert, data);
                index++;
	        }
	    }
	    else
	    {
	        var masterIdName = GeneratedField.MasterIdName;
	        var masterId = GetObjectId(master);
	        int index = 0;
	        for (var slave : slaves)
	        {
	            if (slave.IsNull()) continue;
	            var slaveId = GetObjectId(slave);
	            using (var temp = SqlHelper.BorrowData())
	            {
	                var data = temp.Item;
	                data.Add(rootIdName, rootId);
	                data.Add(masterIdName, masterId);
	                data.Add(slaveIdName, slaveId);
	                data.Add(GeneratedField.OrderIndexName, index);
	                if (this.IsSessionEnabledMultiTenancy)
	                    data.Add(GeneratedField.TenantIdName, AppSession.TenantId);
	                SqlHelper.Execute(this.SqlInsert, data);
	                index++;
	            }
	        }

	    }
	}

	private void InsertMiddleByValues(IDomainObject root, IDomainObject master, IEnumerable values)
	{
	    var rootId = GetObjectId(root);
	    var rootIdName = GeneratedField.RootIdName;
	    if (this.Root.IsEqualsOrDerivedOrInherited(this.Master))
	    {
	        int index = 0;
	        for (var value : values)
	        {
	            using (var temp = SqlHelper.BorrowData())
	            {
	                var data = temp.Item;
	                data.Add(rootIdName, rootId);
	                data.Add(GeneratedField.PrimitiveValueName, value);
	                data.Add(GeneratedField.OrderIndexName, index);
	                if(this.IsSessionEnabledMultiTenancy)
	                    data.Add(GeneratedField.TenantIdName, AppSession.TenantId);
	                SqlHelper.Execute(this.SqlInsert, data);
	                index++;
	            }
	        }
	    }
	    else
	    {
	        var masterIdName = GeneratedField.MasterIdName;
	        var masterId = GetObjectId(master);
	        int index = 0;
	        for (var value in values)
	        {
	            using (var temp = SqlHelper.BorrowData())
	            {
	                var data = temp.Item;
	                data.Add(rootIdName, rootId);
	                data.Add(masterIdName, masterId);
	                data.Add(GeneratedField.PrimitiveValueName, value);
	                data.Add(GeneratedField.OrderIndexName, index);
	                if (this.IsSessionEnabledMultiTenancy)
	                    data.Add(GeneratedField.TenantIdName, AppSession.TenantId);
	                SqlHelper.Execute(this.SqlInsert, data);
	                index++;
	            }
	        }
	    }
	}

	private void insertAndCollectValue(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip, Dictionary data)
	{
	    switch (tip.category())
	    {
	        case DomainPropertyCategory.Primitive:
	            {
	                var value = getPrimitivePropertyValue(current, tip);
	                data.put(tip.PropertyName, value);
	            }
	            break;
	        case DomainPropertyCategory.PrimitiveList:
	            {
	                var value = current.getValue(tip.Property);
	                //仅存中间表
	                var values = GetValueListData(value);
	                var child = GetChildTableByRuntime(this, tip);//无论是派生还是基类，基础表对应的中间表都一样
	                child.InsertMiddle(root, current, values);
	            }
	            break;
	        case DomainPropertyCategory.ValueObject:
	            {
	                InsertAndCollectValueObject(root, parent, current, tip, data);
	            }
	            break;
	        case DomainPropertyCategory.AggregateRoot:
	            {
	                var field = GetQuoteField(this, tip.PropertyName);
	                object obj = current.GetValue(tip.Property);
	                var id = GetObjectId(obj);
	                data.Add(field.Name, id);
	            }
	            break;
	        case DomainPropertyCategory.EntityObject:
	            {
	                var obj = current.GetValue(tip.Property) as DomainObject;

	                var id = GetObjectId(obj);
	                var field = GetQuoteField(this, tip.PropertyName);
	                data.Add(field.Name, id);  //收集外键

	                //保存引用数据
	                if (!obj.IsEmpty())
	                {
	                    var child = GetRuntimeTable(this, tip.PropertyName, obj.ObjectType);
	                    child.InsertMember(root, current, obj);
	                }
	            }
	            break;
	        case DomainPropertyCategory.AggregateRootList:
	            {
	                //仅存中间表
	                var objs = current.GetValue(tip.Property) as IEnumerable;
	                var child = GetChildTableByRuntime(this, tip);//无论是派生还是基类，基础表对应的中间表都一样
	                child.Middle.InsertMiddle(root, current, objs);
	            }
	            break;
	        case DomainPropertyCategory.ValueObjectList:
	        case DomainPropertyCategory.EntityObjectList:
	            {
	                InsertMembers(root, parent, current, tip);
	            }
	            break;
	    }
	}

	private void InsertAndCollectValueObject(DomainObject root, DomainObject parent, DomainObject current, PropertyRepositoryAttribute tip, DynamicData data)
	{
	    var field = GetQuoteField(this, tip.PropertyName);
	    var obj = current.GetValue(tip.Property) as DomainObject;

	    if (obj.IsEmpty())
	    {
	        data.Add(field.Name, Guid.Empty);
	    }
	    else
	    {
	        (obj as IValueObject).TrySetId(Guid.NewGuid());
	        var id = GetObjectId(obj);
	        data.Add(field.Name, id);

	        //保存数据
	        var child = GetRuntimeTable(this, tip.PropertyName, obj.ObjectType);
	        child.InsertMember(root, current, obj);
	    }
	}

	private void InsertMembers(DomainObject root, DomainObject parent, DomainObject current, PropertyRepositoryAttribute tip)
	{
	    var objs = current.GetValue(tip.Property) as IEnumerable;
	    InsertMembers(root, parent, current, objs, tip);
	}

	private void InsertMembers(DomainObject root, DomainObject parent, DomainObject current, IEnumerable members, PropertyRepositoryAttribute tip)
	{
	    DataTable middle = null;
	    for (DomainObject obj : members)
	    {
	        if (obj.IsEmpty()) continue;
	        var child = GetRuntimeTable(this, tip.PropertyName, obj.ObjectType);
	        if (child.Type == DataTableType.ValueObject)
	        {
	            //我们需要为ValueObject补充编号
	            (obj as IValueObject).TrySetId(Guid.NewGuid());
	        }
	        child.InsertMember(root, current, obj);
	        if (middle == null) middle = child.Middle;
	    }
	    if (middle != null) middle.InsertMiddle(root, current, members);
	}

	private String _sqlInsert;

	public String sqlInsert() {
		if (_sqlInsert == null) {
			_sqlInsert = getInsertSql();
		}
		return _sqlInsert;
	}

	private string GetInsertSql() {
		var query = InsertTable.Create(this);
		return query.Build(null, this);
	}
}
