package apros.codeart.ddd.repository.access;

import java.util.function.Consumer;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.Page;

public final class _DataPortal {
	private _DataPortal() {
	}

	/**
	 * 在数据层创建指定对象的数据
	 * 
	 * @param obj
	 */
	public static void insert(DomainObject obj) {
		var objectType = obj.getClass();
		var model = DataModelLoader.get(objectType);
		model.insert(obj);
	}

	/// <summary>
	/// 在数据层中修改指定对象的数据
	/// </summary>
	/// <param name="obj"></param>
	public static void update(DomainObject obj) {
		var objectType = obj.getClass();
		var model = DataModelLoader.get(objectType);
		model.update(obj);
	}

	/// <summary>
	/// 在数据层中删除指定对象的数据
	/// </summary>
	/// <param name="obj"></param>
	public static void delete(DomainObject obj) {
		var objectType = obj.getClass();
		var model = DataModelLoader.get(objectType);
		model.delete(obj);
	}

	/**
	 * 
	 * 在数据层中查找指定编号的数据，并加载到对象实例中
	 * 
	 * @param <T>
	 * @param objectType
	 * @param id
	 * @param level
	 * @return
	 */
	public static <T extends IDomainObject> T querySingle(Class<T> objectType, Object id, QueryLevel level) {
		var model = DataModelLoader.get(objectType);
		return model.querySingle(id, level);
	}

	// 基于对象表达式的查询

	public static <T extends IDomainObject> T querySingle(Class<T> objectType, String expression,
			Consumer<MapData> fillArg, QueryLevel level) {
		var model = DataModelLoader.get(objectType);
		return model.querySingle(expression, fillArg, level);
	}

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>

	public static <T extends IDomainObject> Iterable<T> query(Class<T> objectType, String expression,
			Consumer<MapData> fillArg, QueryLevel level) {
		var model = DataModelLoader.get(objectType);
		return model.query(expression, fillArg, level);
	}

	/**
	 * 
	 * 基于对象表达式的查询
	 * 
	 * @param <T>
	 * @param objectType
	 * @param expression
	 * @param pageIndex
	 * @param pageSize
	 * @param fillArg
	 * @return
	 */
	public static <T extends IDomainObject> Page<T> query(Class<T> objectType, String expression, int pageIndex,
			int pageSize, Consumer<MapData> fillArg) {
		var model = DataModelLoader.get(objectType);
		return model.query(expression, pageIndex, pageSize, fillArg);
	}

	public static <T extends IDomainObject> int getCount(Class<T> objectType, String expression,
			Consumer<MapData> fillArg, QueryLevel level) {
		var model = DataModelLoader.get(objectType);
		return model.getCount(expression, fillArg, level);
	}

//	/**
//	 * 
//	 * 该方法实际上是执行程序员写得自定义命令，exporession由外部识别并执行
//	 * 
//	 * @param <T>
//	 * @param objectType
//	 * @param expression
//	 * @param fillArg
//	 * @param level
//	 * @param fillItems
//	 */
//	public static <T extends IDomainObject> void execute(Class<T> objectType, String expression,
//			Consumer<MapData> fillArg, QueryLevel level, Consumer<Map<String, Object>> fillItems) {
//		var model = DataModelLoader.get(objectType);
//		model.execute(expression, fillArg, level, fillItems);
//	}

}
