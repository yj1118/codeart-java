package apros.codeart.ddd.repository.access;

import java.util.ArrayList;

import apros.codeart.ddd.DomainBuffer;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;

final class DataTableDelete {
	private DataTable _self;

	public DataTableDelete(DataTable self) {
		_self = self;
	}

	void delete(DomainObject obj) {
		if (_self.type() == DataTableType.AggregateRoot) {
			deleteRoot(obj);
			return;
		}

		throw new IllegalStateException(
				Language.strings("codeart.ddd", "PersistentObjectError", obj.getClass().getName()));
	}

	/**
	 * 删除数据
	 * 
	 * @param rootId
	 * @param id
	 * @return
	 */
	private boolean deleteData(Object rootId, Object id) {
		if (_self.type() == DataTableType.AggregateRoot) {
			executeDeleteData(rootId, id);
			return true;
		} else {
			// 删除成员表，成员表会记录引用次数，以此来判定是否真实删除
			var table = _self;

			var associated = table.getAssociated(rootId, id);
			if (associated > 1) {
				table.decrementAssociated(rootId, id);
				return false;
			} else {
				executeDeleteData(rootId, id);
				return true;
			}
		}
	}

	private void onPreDataDelete(DomainObject obj) {
		_self.mapper().onPreDelete(obj, _self);
	}

	/**
	 * 该方法用于删除数据后的补充操作
	 * 
	 * @param rootId
	 * @param id
	 * @param obj
	 */
	private void onDataDeleted(Object rootId, Object id, DomainObject obj) {
		// 从缓冲区中删除对象
		if (_self.type() == DataTableType.AggregateRoot) {
			DomainBuffer.remove(_self.objectType(), rootId); // 不用考虑mirror，删不删除都不影响
		}
		_self.mapper().onDeleted(obj, _self);
	}

	private void executeDeleteData(Object rootId, Object id) {
		var data = new MapData();
		data.put(GeneratedField.RootIdName, rootId);
		data.put(EntityObject.IdPropertyName, id);

		var sql = getDeleteSql();
		DataAccess.getCurrent().execute(sql, data);
	}

	private String getDeleteSql() {
		var qb = DataSource.getQueryBuilder(DeleteTableQB.class);
		return qb.build(new QueryDescription(_self));
	}

//	#region 删除根对象

	private void deleteRoot(DomainObject obj) {
		if (obj == null || obj.isEmpty())
			return;

		var ar = TypeUtil.as(obj, IAggregateRoot.class);
		if (ar == null)
			throw new IllegalStateException(Language.strings("codeart.ddd", "CanNotDeleteNonAggregateRoot"));

		_self.checkDataVersion(obj);

		var rootId = ar.getIdentity();
		onPreDataDelete(obj);
		if (deleteData(rootId, rootId)) {
			deleteMiddles(obj);
			deleteMembers(obj, obj, obj);
			onDataDeleted(rootId, rootId, obj);
		}

	}

	private void deleteMiddles(DomainObject obj) {
		var middles = RootIsSlaveIndex.get(_self);
		for (var middle : middles) {
			middle.deleteMiddleByRootSlave(obj);
		}
	}

//	region 删除成员

	/// <summary>
	/// 删除目标对象<paramref name="parent"/>的成员数据
	/// </summary>
	/// <param name="root"></param>
	/// <param name="parent"></param>
	private void deleteMembers(DomainObject root, DomainObject parent, DomainObject current) {
		// AggregateRoot，由程序员手工调用删除

		var tips = PropertyMeta.getProperties(current.getClass());
		for (var tip : tips) {
			switch (tip.category()) {
			case DomainPropertyCategory.EntityObject:
			case DomainPropertyCategory.ValueObject: {
				deleteMemberByPropertyValue(root, parent, current, tip);
			}
				break;
			case DomainPropertyCategory.EntityObjectList:
			case DomainPropertyCategory.ValueObjectList: {
				deleteMembersByPropertyValue(root, parent, current, tip);
			}
				break;
			case DomainPropertyCategory.PrimitiveList: {
				// 删除老数据
				var child = _self.findChild(_self, tip);
				child.deleteMiddleByMaster(root, current);
			}
				break;
			case DomainPropertyCategory.AggregateRootList: {
				// 仅删除引用关系（也就是中间表数据），由于不需要删除slave根表的数据，因此直接使用该方法更高效，不需要读取实际集合值
				deleteQuotesByMaster(root, current);
			}
				break;
			default:
				break;
			}
		}
	}

	/// <summary>
	/// 根据对象当前定义的属性，删除成员
	/// </summary>
	/// <param name="root"></param>
	/// <param name="parent"></param>
	/// <param name="current"></param>
	/// <param name="tip"></param>
	private void deleteMembersByPropertyValue(DomainObject root, DomainObject parent, DomainObject current,
			PropertyMeta tip) {
		var objs = (Iterable<?>) current.getValue(tip.name());
		deleteMembers(root, parent, current, objs, tip);
	}

	/// <summary>
	/// 从数据库中加载成员，并且删除
	/// </summary>
	/// <param name="root"></param>
	/// <param name="parent"></param>
	/// <param name="current"></param>
	/// <param name="tip"></param>
	public void deleteMembersByOriginalData(DomainObject root, DomainObject parent, DomainObject current,
			PropertyMeta tip) {
		var originalData = DataTableUtil.getOriginalData(current);

		var objs = new ArrayList<Object>();
		_self.queryMembers(tip, originalData, objs);
		deleteMembers(root, parent, current, objs, tip);
	}

	/// <summary>
	/// 该方法是一个重构类型的工具方法，删除<paramref name="current"/>上的属性数据，属性类型为对象的集合
	/// </summary>
	/// <param name="root"></param>
	/// <param name="parent"></param>
	/// <param name="members"></param>
	/// <param name="tip"></param>
	void deleteMembers(DomainObject root, DomainObject parent, DomainObject current, Iterable<?> members,
			PropertyMeta tip) {
		var propertyName = tip.name();
		for (var member : members) {
			var obj = (DomainObject) member;
			if (obj.isEmpty())
				continue;
			var child = _self.findChild(_self, propertyName, obj.getClass());
			// 删除对象的数据
			child.deleteMember(root, current, obj);
			child.middle().deleteMiddle(root, current, obj);
		}
	}

	// 由于两个原因，我们不会删除根对象后，去反向找哪些表的字段引用了该根对象，
	// 1.领域有空对象的概念，引用的根对象没了后，加载的是空对象
	// 2.不同的子系统物理分布都不一样，表都在不同的数据库里，没有办法也不需要去追踪
	// 因此DeleteQuotesBySlave我们用不上
	///// <summary>
	///// 删除引用了<paramref name="slave"/>数据的中间表数据
	///// </summary>
	///// <param name="root"></param>
	///// <param name="slave"></param>
	// private void DeleteQuotesBySlave(DomainObject root, DomainObject slave)
	// {
	// var quotes = GetQuoteMiddlesBySlave(this);
	// foreach (var quote in quotes)
	// {
	// quote.DeleteMiddleBySlave(root, slave);
	// }
	// }

	/**
	 * 
	 * 删除引用了 {@code master} 数据的中间表数据
	 * 
	 * @param root
	 * @param master
	 */
	private void deleteQuotesByMaster(DomainObject root, DomainObject master) {
		var quotes = _self.getQuoteMiddlesByMaster();
		for (var quote : quotes) {
			quote.deleteMiddleByMaster(root, master);
		}
	}

	/**
	 * 
	 * 根据当前属性值，删除成员
	 * 
	 * @param root
	 * @param parent
	 * @param current
	 * @param tip
	 */
	private void deleteMemberByPropertyValue(DomainObject root, DomainObject parent, DomainObject current,
			PropertyMeta tip) {
		var obj = (DomainObject) current.getValue(tip.name());
		if (!obj.isEmpty()) {
			var child = _self.findChild(_self, tip.name(), obj.getClass());
			child.deleteMember(root, parent, obj);
		}
	}

	/**
	 * 
	 * 从数据库中加载成员，并且删除
	 * 
	 * @param root
	 * @param parent
	 * @param current
	 * @param tip
	 */
	public void deleteMemberByOriginalData(DomainObject root, DomainObject parent, DomainObject current,
			PropertyMeta tip) {
		var originalData = DataTableUtil.getOriginalData(current);
		var obj = (DomainObject) _self.queryMember(tip, originalData);
		if (!obj.isEmpty()) {
			var child = _self.findChild(_self, tip.name(), obj.getClass());
			child.deleteMember(root, parent, obj);
		}
	}

	/**
	 * 
	 * 删除成员（该方法用于删除 ValueObject,EntityObject等对象）
	 * 
	 * @param root
	 * @param parent
	 * @param obj
	 */
	void deleteMember(DomainObject root, DomainObject parent, DomainObject obj) {
		if (obj == null || obj.isEmpty())
			return;

		var rootId = DataTableUtil.getObjectId(root);
		var id = DataTableUtil.getObjectId(obj);

		onPreDataDelete(obj);
		if (deleteData(rootId, id)) {
			deleteMembers(root, parent, obj);
			onDataDeleted(rootId, id, obj);
		}
	}

//	#endregion

//	region 删除中间表数据

	void deleteMiddleByMaster(DomainObject root, DomainObject master) {
		deleteMiddle(root, master, null);
	}

	void deleteMiddleByRootSlave(DomainObject slave) {
		// 根据slave删除中间表数据
		var slaveId = DataTableUtil.getObjectId(slave);
		var data = new MapData();
		data.put(GeneratedField.RootIdName, null);
		data.put(GeneratedField.MasterIdName, null);
		data.put(GeneratedField.SlaveIdName, slaveId);

		var sql = getDeleteSql();
		DataAccess.getCurrent().execute(sql, data);
	}

	void deleteMiddle(DomainObject root, DomainObject master, DomainObject slave) {
		if (_self.isPrimitiveValue()) {
			deleteMiddleByValues(root, master);
			return;
		}

		// 中间表直接删除自身的数据
		var rootId = DataTableUtil.getObjectId(root);
		var slaveId = slave == null ? null : DataTableUtil.getObjectId(slave);

		if (_self.root().same(_self.master())) {
			var data = new MapData();
			data.put(GeneratedField.RootIdName, rootId);
			data.put(GeneratedField.SlaveIdName, slaveId);// slaveId有可能为空，因为是根据master删除，但是没有关系，sql会处理slaveId为空的情况

			var sql = this.getDeleteSql();
			DataAccess.getCurrent().execute(sql, data);
		} else {
			var masterId = master == null ? null : DataTableUtil.getObjectId(master);
			var data = new MapData();
			data.put(GeneratedField.RootIdName, rootId);
			data.put(GeneratedField.MasterIdName, masterId);
			data.put(GeneratedField.SlaveIdName, slaveId);

			var sql = this.getDeleteSql();
			DataAccess.getCurrent().execute(sql, data);
		}
	}

	private void deleteMiddleByValues(DomainObject root, DomainObject master) {
		// 中间表直接删除自身的数据
		var rootId = DataTableUtil.getObjectId(root);

		if (_self.root().same(_self.master())) {
			var data = new MapData();
			data.put(GeneratedField.RootIdName, rootId);

			var sql = this.getDeleteSql();
			DataAccess.getCurrent().execute(sql, data);
		} else {
			var masterId = master == null ? null : DataTableUtil.getObjectId(master);
			var data = new MapData();
			data.put(GeneratedField.RootIdName, rootId);
			data.put(GeneratedField.MasterIdName, masterId);

			var sql = this.getDeleteSql();
			DataAccess.getCurrent().execute(sql, data);
		}
	}

//	#endregion

}
