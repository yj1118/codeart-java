package apros.codeart.ddd.repository.access;

import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.TestSupport;
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

    public static <T extends IDomainObject> T querySingle(Class<T> objectType, String expression, QueryLevel level) {
        var model = DataModelLoader.get(objectType);
        return model.querySingle(expression, null, level);
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

    private static long getIdentity(DataTable table) {
        return DataContext.newScope((conn) -> {

            var builder = DataSource.getQueryBuilder(GetIncrIdQB.class);
            var sql = builder.build(new QueryDescription(table));

            return conn.access().nativeQueryScalarLong(sql);
        });
    }

    /**
     * 根据对象类型，获取一个自增的编号，该编号由数据层维护递增
     *
     * @param <T>
     * @param doType
     * @return
     */
    public static <T extends IDomainObject> long getIdentity(Class<T> doType) {

        var table = DataTableLoader.get(doType);
        return getIdentity(table);
    }

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
        return direct((conn) -> {
            return conn.access().queryRows(sql, param);
        });
    }

    /**
     * 直接使用数据库连接操作数据库
     *
     * @param action
     */
    public static void direct(Consumer<DataConnection> action) {
        DataContext.using(action);
    }

    public static <T> T direct(Function<DataConnection, T> action) {
        return DataContext.using(action);
    }

    /**
     * 在数据层创建指定对象的数据
     *
     * @param obj
     */
    static void insert(DomainObject obj) {
        var objectType = obj.getClass();
        var model = DataModelLoader.get(objectType);
        model.insert(obj);
    }

    /// <summary>
    /// 在数据层中修改指定对象的数据
    /// </summary>
    /// <param name="obj"></param>
    static void update(DomainObject obj) {
        var objectType = obj.getClass();
        var model = DataModelLoader.get(objectType);
        model.update(obj);
    }

    /// <summary>
    /// 在数据层中删除指定对象的数据
    /// </summary>
    /// <param name="obj"></param>
    static void delete(DomainObject obj) {
        var objectType = obj.getClass();
        var model = DataModelLoader.get(objectType);
        model.delete(obj);
    }

}
