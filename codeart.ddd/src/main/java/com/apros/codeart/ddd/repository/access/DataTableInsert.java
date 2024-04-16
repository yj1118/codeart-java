package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.DomainBuffer;
import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.IValueObject;
import com.apros.codeart.ddd.MapData;
import com.apros.codeart.ddd.metadata.DomainPropertyCategory;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.Guid;
import com.apros.codeart.util.ObjectUtil;
import com.apros.codeart.util.StringUtil;

final class DataTableInsert {

	private DataTable _self;

	public DataTableInsert(DataTable self) {
		_self = self;
	}

	public void insert(DomainObject obj) {
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
	private MapData insertData(DomainObject root, DomainObject parent, DomainObject obj) {
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
	private void onDataInserted(DomainObject root, DomainObject obj, MapData objData) {
		if (_self.type() == DataTableType.AggregateRoot) {
			var ar = (IAggregateRoot) obj;
			DomainBuffer.add(ar);
		}

		_self.mapper().onInserted(obj, _self);
	}

	private MapData getInsertData(DomainObject root, DomainObject parent, DomainObject obj) {
		Class<?> objectType = _self.objectType();

		var tips = PropertyMeta.getProperties(objectType);
		var data = new MapData();
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
		default:
			break;
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
	public void insertMember(DomainObject root, DomainObject parent, DomainObject obj) {
		if (obj == null || obj.isEmpty())
			return;

		// 我们需要先查，看数据库中是否存在数据，如果不存在就新增，存在就增加引用次数
		var existObject = _self.querySingle(DataTableUtil.getObjectId(root), DataTableUtil.getObjectId(obj));

		if (ObjectUtil.isNull(existObject)) {
			onPreDataInsert(obj);
			var data = insertData(root, parent, obj);
			onDataInserted(root, obj, data);
		} else {
			// 递增引用次数
			incrementAssociated(DataTableUtil.getObjectId(root), DataTableUtil.getObjectId(obj));
		}
	}

	/// <summary>
	/// 递增引用次数
	/// </summary>
	/// <param name="root"></param>
	/// <param name="parent"></param>
	/// <param name="obj"></param>
	private void incrementAssociated(Object rootId, Object id) {
		var data = new MapData();
		data.put(GeneratedField.RootIdName, rootId);
		data.put(EntityObject.IdPropertyName, id);

		var builder = DataSource.getQueryBuilder(IncrementAssociatedQB.class);
		var sql = builder.build(new QueryDescription(_self));
		DataAccess.getCurrent().execute(sql, data);
	}

	public void insertMiddle(IDomainObject root, IDomainObject master, Iterable<?> slaves) {
		if (_self.isPrimitiveValue()) {
			insertMiddleByValues(root, master, slaves);
			return;
		}

		var rootId = DataTableUtil.getObjectId(root);
		var rootIdName = GeneratedField.RootIdName;
		var slaveIdName = GeneratedField.SlaveIdName;

		if (_self.masterIsRoot()) {
			int index = 0;
			for (var slave : slaves) {
				if (ObjectUtil.isNull(slave))
					continue;
				var slaveId = DataTableUtil.getObjectId(slave);
				var data = new MapData();
				data.put(rootIdName, rootId);
				data.put(slaveIdName, slaveId);
				data.put(GeneratedField.OrderIndexName, index);

				DataAccess.getCurrent().execute(this.sqlInsert(), data);
				index++;
			}
		} else {
			var masterIdName = GeneratedField.MasterIdName;
			var masterId = DataTableUtil.getObjectId(master);
			int index = 0;
			for (var slave : slaves) {
				if (ObjectUtil.isNull(slave))
					continue;
				var slaveId = DataTableUtil.getObjectId(slave);
				var data = new MapData();
				data.put(rootIdName, rootId);
				data.put(masterIdName, masterId);
				data.put(slaveIdName, slaveId);
				data.put(GeneratedField.OrderIndexName, index);

				DataAccess.getCurrent().execute(this.sqlInsert(), data);
				index++;
			}

		}
	}

	private void insertMiddleByValues(IDomainObject root, IDomainObject master, Iterable<?> values) {
		var rootId = DataTableUtil.getObjectId(root);
		var rootIdName = GeneratedField.RootIdName;
		if (_self.masterIsRoot()) {
			int index = 0;
			for (var value : values) {
				var data = new MapData();
				data.put(rootIdName, rootId);
				data.put(GeneratedField.PrimitiveValueName, value);
				data.put(GeneratedField.OrderIndexName, index);
				DataAccess.getCurrent().execute(this.sqlInsert(), data);
				index++;
			}
		} else {
			var masterIdName = GeneratedField.MasterIdName;
			var masterId = DataTableUtil.getObjectId(master);
			int index = 0;
			for (var value : values) {
				var data = new MapData();
				data.put(rootIdName, rootId);
				data.put(masterIdName, masterId);
				data.put(GeneratedField.PrimitiveValueName, value);
				data.put(GeneratedField.OrderIndexName, index);
				DataAccess.getCurrent().execute(this.sqlInsert(), data);
				index++;
			}
		}
	}

	private void insertAndCollectValue(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip,
			MapData data) {
		switch (tip.category()) {
		case DomainPropertyCategory.Primitive: {
			var value = DataTableUtil.getPrimitivePropertyValue(current, tip);
			data.put(tip.name(), value);
		}
			break;
		case DomainPropertyCategory.PrimitiveList: {
			var value = current.getValue(tip.name());
			// 仅存中间表
			var values = DataTableUtil.getValueListData(value, tip.monotype());
			var child = _self.findChild(_self, tip);// 无论是派生还是基类，基础表对应的中间表都一样
			child.insertMiddle(root, current, values);
		}
			break;
		case DomainPropertyCategory.ValueObject: {
			insertAndCollectValueObject(root, parent, current, tip, data);
		}
			break;
		case DomainPropertyCategory.AggregateRoot: {
			var field = DataTableUtil.getQuoteField(_self, tip.name());
			Object obj = current.getValue(tip.name());
			var id = DataTableUtil.getObjectId(obj);
			data.put(field.name(), id);
		}
			break;
		case DomainPropertyCategory.EntityObject: {
			var obj = (DomainObject) current.getValue(tip.name());

			var id = DataTableUtil.getObjectId(obj);
			var field = DataTableUtil.getQuoteField(_self, tip.name());
			data.put(field.name(), id); // 收集外键

			// 保存引用数据
			if (!obj.isEmpty()) {
				var child = _self.findChild(_self, tip.name(), obj.getClass());
				child.insertMember(root, current, obj);
			}
		}
			break;
		case DomainPropertyCategory.AggregateRootList: {
			// 仅存中间表
			var objs = TypeUtil.as(current.getValue(tip.name()), Iterable.class);
			var child = _self.findChild(_self, tip);// 基础表对应的中间表都一样
			child.middle().insertMiddle(root, current, objs);
		}
			break;
		case DomainPropertyCategory.ValueObjectList:
		case DomainPropertyCategory.EntityObjectList: {
			insertMembers(root, parent, current, tip);
		}
			break;
		}
	}

	private void insertAndCollectValueObject(DomainObject root, DomainObject parent, DomainObject current,
			PropertyMeta tip, MapData data) {
		var field = DataTableUtil.getQuoteField(_self, tip.name());
		var obj = TypeUtil.as(current.getValue(tip.name()), DomainObject.class);

		if (obj.isEmpty()) {
			data.put(field.name(), Guid.Empty);
		} else {
			((IValueObject) obj).setPersistentIdentity(Guid.NewGuid());
			var id = DataTableUtil.getObjectId(obj);
			data.put(field.name(), id);

			// 保存数据
			var child = _self.findChild(_self, tip.name(), obj.getClass());
			child.insertMember(root, current, obj);
		}
	}

	void insertMembers(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip) {
		var objs = TypeUtil.as(current.getValue(tip.name()), Iterable.class);
		insertMembers(root, parent, current, objs, tip);
	}

	void insertMembers(DomainObject root, DomainObject parent, DomainObject current, Iterable<?> members,
			PropertyMeta tip) {
		DataTable middle = null;
		for (var member : members) {
			var obj = (DomainObject) member;
			if (obj.isEmpty())
				continue;
			var child = _self.findChild(_self, tip.name(), obj.getClass());
			if (child.type() == DataTableType.ValueObject) {
				// 我们需要为ValueObject补充编号
				((IValueObject) obj).setPersistentIdentity(Guid.NewGuid());
			}
			child.insertMember(root, current, obj);
			if (middle == null)
				middle = child.middle();
		}
		if (middle != null)
			middle.insertMiddle(root, current, members);
	}

	private String _sqlInsert;

	public String sqlInsert() {
		if (_sqlInsert == null) {
			_sqlInsert = getInsertSql();
		}
		return _sqlInsert;
	}

	private String getInsertSql() {
		var builder = DataSource.getQueryBuilder(InsertTableQB.class);
		return builder.build(new QueryDescription(_self));
	}
}
