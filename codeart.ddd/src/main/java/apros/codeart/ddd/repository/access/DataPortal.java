package apros.codeart.ddd.repository.access;

import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Page;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.EmptyEventArgs;
import apros.codeart.util.EventHandler;

public final class DataPortal {

	private DataPortal() {
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

//	#region 对外公开的方法

	/**
	 * 
	 * 
	 * 
	 * @param tableName
	 * @return
	 */
	public static long getIdentity(String tableName) {
		return DataContext.newScope((access) -> {
			String sql = SqlStatement.getIncrIdSql(tableName);
			return access.queryScalarLong(sql);
		});
	}

	/**
	 * 
	 * 根据对象类型，获取一个自增的编号，该编号由数据层维护递增
	 * 
	 * @param <T>
	 * @param doType
	 * @return
	 */
	public static <T extends IDomainObject> long getIdentity(Class<T> doType) {

		var table = DataTableLoader.get(doType);
		return getIdentity(table.name());
	}

// todo 似乎没用了
//	/// <summary>
//	/// 创建运行时记录的表信息，这在关系数据库中体现为创建表
//	/// 该方法一般用于单元测试
//	/// </summary>
//	/// <typeparam name="T"></typeparam>
//	public static void RuntimeBuild() {
//		DataModel.RuntimeBuild();
//	}

//	/**
//	 * 
//	 * 初始化 <typeparamref name="T"/> 对应的数据模型，这会初始化表结构
//	 * 
//	 * @param <T>
//	 */
//	public static <T extends IAggregateRoot> void init(Class<T> rootType) {
//		DataModel.create(rootType);
//	}

//	#region 销毁数据

	/**
	 * 在数据层中销毁数据模型
	 */
	public static void dispose() {
		DataModel.drop();
	}

//	#endregion

	/**
	 * 清理数据，但是不销毁数据模型
	 */
	public static void clearUp() {
		DataModel.clearUp();
		onClearUp.raise(null, () -> EmptyEventArgs.Instance);
	}

	public static EventHandler<EmptyEventArgs> onClearUp = new EventHandler<EmptyEventArgs>();

	public static Iterable<MapData> directQuery(String sql, MapData param) {
		return direct((access) -> {
			return access.queryRows(sql, param);
		});
	}

	/**
	 * 直接使用数据库连接操作数据库
	 * 
	 * @param objectType
	 * @param action
	 */
	public static void direct(Consumer<DataAccess> action) {
		DataContext.using(action);
	}

	public static <T> T direct(Function<DataAccess, T> action) {
		return DataContext.using(action);
	}

//	#endregion

//	#region keep
//
//	 todo 新版中的数据代理取消了数据存在会话里的机制，所以不需要keep了
//	/// <summary>
//	/// 赋予新建的对象可以持续存在多个线程之中，
//	/// 有时候，我们需要建立领域对象，但是并不存入数据库，而是作为内存中的缓存使用，
//	/// 这时候在创建对象后，就需要将对象keep下，才能在随后的多个线程里正常访问，
//	/// 这里的原因是，引用的根对象是存在线程栈里的，在别的线程访问时，没有本地数据做支持就无法延迟加载，所以需要数据层处理
//	/// </summary>
//	/// <typeparam name="T"></typeparam>
//	/// <param name="obj"></param>
//	/// <returns></returns>
//	public static void Keep<T>(T obj)
//	where T:DomainObject
//	{
//	     var objectType = obj.ObjectType;
//	     var table = DataTable.GetTable<T>();
//	     if (table == null) throw new ApplicationException("没有找到" + objectType.FullName + "对应的表定义");
//	     table.Keep(obj);
//	 }
//
//	#endregion

//	/// <summary>
//	/// 将指定的对象从缓存区从移除
//	/// </summary>
//	/// <typeparam name="T"></typeparam>
//	/// <param name="id"></param>
//	public static void Off<T>(object id)
//	where T:DomainObject
//	{
//		var objectType = typeof(T);
//		DomainBuffer.Public.Remove(objectType, id);
//	}

}
