package com.apros.codeart.ddd.repository.access;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.apros.codeart.ddd.Dictionary;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.repository.DataContext;
import com.apros.codeart.ddd.repository.Page;
import com.apros.codeart.util.WrapperInt;
import com.apros.codeart.util.WrapperLong;

public final class DataPortal {

	private DataPortal() {
	}

//	#region 对外公开的方法

	/**
	 * 
	 * 根据对象类型，获取一个自增的编号，该编号由数据层维护递增
	 * 
	 * @param tableName
	 * @return
	 */
	public static long getIdentity(String tableName) {
		var id = new WrapperLong();
		DataContext.newScope((access) -> {
			String sql = DataSource.getAgent().getIncrIdSql(tableName);
			id.set(access.queryScalarLong(sql));
		});
		return id.get();
	}

	public static <T extends IAggregateRoot> long getIdentity(Class<?> rootType) {

		var model = DataModel.get(rootType);

		var id = new WrapperLong();
		DataContext.newScope(() -> {
			id.set(model.getIdentity());
		});
		return id.get();
	}

	/**
	 * 获得流水号,流水号保证对每个租户都是连续的
	 * 
	 * 请注意，流水号不能用于领域对象的编号，因为编号必须保证全局唯一
	 * 
	 * @param <T>
	 * @param rootType
	 * @return
	 */
	public static <T extends IAggregateRoot> long getSerialNumber(Class<?> rootType) {
	{
	     var model = DataModel.get(rootType);

	     long sn = 0;
	     DataContext.newScope(() ->
	     {
	    	 sn = model.getSerialNumber();
	     });
	     return sn;
	 }

	/**
	 * 可以为非根对象建立唯一标示
	 * 
	 * @param <A>
	 * @param <M>
	 * @return
	 */
	public static <A extends IAggregateRoot,M extends DomainObject> long getIdentity()
	{
	     var objectType = typeof(A);
	     var model = DataModel.Create(objectType);

	     var table = DataTable.GetTable<T>();

	     long id = 0;
	     DataContext.UseTransactionScope(() ->
	     {
	         id = table.GetIdentity();
	     });
	     return id;
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

	/**
	 * 
	 * 初始化 <typeparamref name="T"/> 对应的数据模型，这会初始化表结构
	 * 
	 * @param <T>
	 */
	public static <T extends IAggregateRoot> void init(Class<T> rootType) {
		DataModel.create(rootType);
	}

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
		for (var evt : _onClearUpEvents) {
			evt();
		}
	}

	private static ArrayList<Runnable> _onClearUpEvents = new ArrayList<Runnable>();

	public static void onClearUp(Action action) {
		_onClearUpEvents.Add(action);
	}

	public static Iterable<Dictionary> directQuery(String sql, Dictionary param)
	{
	     IEnumerable<Dictionary> result = null;
	     direct<T>((access) ->
	     {
	         result = access.Query(sql, param);
	     });
	     return result;
	 }

	/**
	 * 直接使用数据库连接操作数据库
	 * 
	 * @param objectType
	 * @param action
	 */
	public static void direct(Consumer<DataAccess> action) {
		var model = DataModel.Create(objectType);
		DataContext.Using(() -> {
			var conn = DataContext.Current.Connection;
			action(conn);
		});
	}

	public static void direct(Consumer<DataAccess> action) {
		DataContext.using((access) -> {
			action(access);
		});
	}

//	#endregion

	/// <summary>
	/// 在数据层创建指定对象的数据
	/// </summary>
	/// <param name="obj"></param>
	static void Create(DomainObject obj) {
		var objectType = obj.GetType();
		var model = DataModel.Create(objectType);
		model.Insert(obj);
	}

	/// <summary>
	/// 在数据层中修改指定对象的数据
	/// </summary>
	/// <param name="obj"></param>
	internal

	static void Update(DomainObject obj) {
		Type objectType = obj.GetType();
		var model = DataModel.Create(objectType);
		model.Update(obj);
	}

	/// <summary>
	/// 在数据层中删除指定对象的数据
	/// </summary>
	/// <param name="obj"></param>
	internal

	static void Delete(DomainObject obj) {
		var objectType = obj.GetType();
		var model = DataModel.Create(objectType);
		model.Delete(obj);
	}

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
	static <T extends IEntityObject> T querySingle(Class<T> objectType, Object id, QueryLevel level) {
		var model = DataModel.get(objectType);
		return model.querySingle(objectType, id, level);
	}

	// 基于对象表达式的查询

	static <T extends IDomainObject> T querySingle(Class<T> objectType, String expression, Consumer<Dictionary> fillArg,
			QueryLevel level) {

		var model = DataModel.get(objectType);
		return model.querySingle(expression, fillArg, level);
	}

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>

	static <T extends IDomainObject> Iterable<T> query(Class<T> objectType, String expression,
			Consumer<Dictionary> fillArg, QueryLevel level) {
		var model = DataModel.get(objectType);
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
	static <T extends IDomainObject> Page<T> query(Class<T> objectType, String expression, int pageIndex, int pageSize,
			Consumer<Dictionary> fillArg) {
		var model = DataModel.get(objectType);
		return model.query(expression, pageIndex, pageSize, fillArg);
	}

	static <T extends IDomainObject> int getCount(Class<T> objectType, String expression, Consumer<Dictionary> fillArg,
			QueryLevel level) {
		var model = DataModel.get(objectType);
		return model.getCount(expression, fillArg, level);
	}

	static <T extends IDomainObject> void execute(Class<T> objectType, String expression, Consumer<Dictionary> fillArg,
			QueryLevel level) {
		var model = DataModel.get(objectType);
		model.execute(expression, fillArg, level);
	}

}
