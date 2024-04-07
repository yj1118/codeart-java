package com.apros.codeart.ddd.repository.access;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.apros.codeart.ddd.Dictionary;
import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.metadata.ObjectMeta;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.repository.Page;
import com.apros.codeart.util.LazyIndexer;

final class DataModel {

	public Class<?> objectType() {
		return _objectMeta.objectType();
	}

	private DataTable _root;

	/**
	 * 根表的信息
	 * 
	 * @return
	 */
	public DataTable root() {
		return _root;
	}

	private ObjectMeta _objectMeta;

	public ObjectMeta objectMeta() {
		return _objectMeta;
	}

	DataModel(ObjectMeta objectMeta, DataTable root) {
		_objectMeta = objectMeta;
		_root = root;
	}

	public void insert(DomainObject obj) {
		_root.insert(obj);
	}

	public void update(DomainObject obj) {
		_root.update(obj);
	}

	public void delete(DomainObject obj) {
		_root.delete(obj);
	}

	public <T extends IDomainObject> T querySingle(Object id, QueryLevel level) {
		return (T) _root.querySingle(id, level);
	}

	public <T extends IDomainObject> T querySingle(String expression, Consumer<Dictionary> fillArg, QueryLevel level) {
		return (T) _root.querySingle(expression, fillArg, level);
	}

	public <T extends IDomainObject> Iterable<T> query(String expression, Consumer<Dictionary> fillArg,
			QueryLevel level) {
		return _root.query(expression, fillArg, level);
	}

	public <T extends IDomainObject> Page<T> Query(String expression, int pageIndex, int pageSize,
			Consumer<Dictionary> fillArg) {
		return _root.query(expression, pageIndex, pageSize, fillArg);
	}

	public int getCount(String expression, Consumer<Dictionary> fillArg, QueryLevel level) {
		return _root.getCount(expression, fillArg, level);
	}

	public void execute(String expression, Consumer<Dictionary> fillArg, QueryLevel level) {
		_root.execute(expression, fillArg, level);
	}

	/// <summary>
	/// 获取一个自增的编号
	/// </summary>
	/// <returns></returns>
	public long getIdentity() {
		return _root.getIdentity();
	}

	public long getSerialNumber() {
		return _root.getSerialNumber();
	}

	///// <summary>
	///// 初始化数据模型，这在关系数据库中体现为创建表
	///// <para>该方法适合单元测试的时候重复使用</para>
	///// </summary>
	public static void RuntimeBuild() {
		DataTable.RuntimeBuild();
	}

	public static void Drop() {
		DataTable.Drop();
	}

	public static void ClearUp() {
		DataTable.ClearUp();
	}

	/// <summary>
	/// 找出当前应用程序可能涉及到的表信息
	/// </summary>
	private static IEnumerable<string> _indexs;

	static DataModel() {
		try {
			DomainObject.Initialize();
			_indexs = GetIndexs();
		} catch (Exception ex) {
			throw ex;
		}
	}

	private static IEnumerable<string> GetIndexs()
	 {
	     DomainObject.CheckInitialized();

	     List<string> tables = new List<string>();
	     foreach (var objectType in DomainObject.TypeIndex)
	     {
	         if (DomainObject.IsEmpty(objectType)) continue;
	         var mapper = DataMapperFactory.Create(objectType);
	         var fileds = mapper.GetObjectFields(objectType, false);
	         tables.Add(objectType.Name);
	         tables.AddRange(DataTable.GetRelatedNames(objectType, fileds));
	     }
	     return tables.Distinct();
	 }

}
