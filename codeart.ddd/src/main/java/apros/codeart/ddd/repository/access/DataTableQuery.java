package apros.codeart.ddd.repository.access;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Iterables;

import apros.codeart.ddd.DataVersionException;
import apros.codeart.ddd.DomainBuffer;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.Page;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

final class DataTableQuery {

    private DataTable _self;

    public DataTableQuery(DataTable self) {
        _self = self;
    }

    // 根据编号查询数据
    public Object querySingle(Object id, QueryLevel level) {
        var obj = getObjectFromConstruct(id);
        if (obj != null)
            return obj;

        var expression = getObjectByIdExpression(_self);
        return querySingle(expression, (param) -> {
            param.put(EntityObject.IdPropertyName, id);
        }, level);
    }

    /// <summary>
    /// 1对1引用关系的读取
    /// </summary>
    /// <param name="rootId"></param>
    /// <param name="id"></param>
    /// <returns></returns>
    @SuppressWarnings("unchecked")
    public <T> T querySingle(Object rootId, Object id) {
        var obj = getObjectFromConstruct(rootId, id);
        if (obj != null)
            return (T) obj;

        var expression = getObjectByIdExpression(_self);
        return querySingle(expression, (param) -> {
            param.put(GeneratedField.RootIdName, rootId);
            param.put(EntityObject.IdPropertyName, id);
        }, QueryLevel.None);
    }

//	#region 从构造上下文中获取对象

    /// <summary>
    /// 从构造上下文中获取对象
    /// </summary>
    /// <returns></returns>
    Object getObjectFromConstruct(Object id) {
        return ConstructContext.get(_self.objectType(), id);
    }

    /// <summary>
    /// 从构造上下文中获取对象
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>
    Object getObjectFromConstruct(Object rootId, Object id) {
        return ConstructContext.get(_self.objectType(), rootId, id);
    }

    Object getObjectFromConstruct(MapData data) {
        var id = data.get(EntityObject.IdPropertyName);
        if (_self.type() == DataTableType.AggregateRoot) {
            return getObjectFromConstruct(id);
        }
        var rootId = data.get(GeneratedField.RootIdName);
        return getObjectFromConstruct(rootId, id);
    }

//	#
//
//	endregion

    /// <summary>
    /// 根据表达式查询单条数据
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="expression"></param>
    /// <param name="fillArg"></param>
    /// <param name="level"></param>
    /// <returns></returns>
    @SuppressWarnings("unchecked")
    <T> T querySingle(String expression, Consumer<MapData> fillArg, QueryLevel level) {
        expression = getEntryExpression(expression);

        var query = DataSource.getQueryBuilder(QueryObjectQB.class);

        Object result = null;

        var data = executeQueryEntry(query, expression, level, fillArg);
        if (data.size() > 0) {
            result = getObjectFromEntry(data, level);
        }

        return (T) (result != null ? result : DomainObject.getEmpty(_self.objectType()));
    }

    /// <summary>
    /// 该方法用于快速加载对象
    /// </summary>
    /// <returns></returns>
    private Object getObjectFromEntryByPage(MapData entry) {
        String typeKey = (String) entry.get(GeneratedField.TypeKeyName);
        var table = StringUtil.isNullOrEmpty(typeKey) ? _self : DataTableUtil.getDataTable(typeKey);

        // 非聚合根是不能被加入到缓冲区的
        return table.createObject(table.objectType(), entry, QueryLevel.None);
    }

    /// <summary>
    /// 从条目数据中获取对象
    /// <para>如果加载的对象是聚合根，那么如果存在缓冲区中，那么使用缓冲区的对象，如果不存在，则在数据库中查询并更新缓冲区</para>
    /// <para>如果加载的是成员对象，那么始终从数据库中查询</para>
    /// <para>不论是什么类型的对象都可以识别继承类型</para>
    /// </summary>
    /// <returns></returns>
    private Object getObjectFromEntry(MapData entry, QueryLevel level) {
        if (_self.type() == DataTableType.AggregateRoot) {
            Object id = entry.get(EntityObject.IdPropertyName);
            String typeKey = (String) entry.get(GeneratedField.TypeKeyName);
            var table = StringUtil.isNullOrEmpty(typeKey) ? _self : DataTableUtil.getDataTable(typeKey);

            if (level.code() == QueryLevel.MirroringCode) {
                // 镜像查询会在加入到当前会话中的对象缓冲区，不同的会话有各自的镜像对象，同一个会话的同一个对象的镜像只有一个
                return DomainBuffer.obtain(table.objectType(), id, () -> {
                    return (IAggregateRoot) table.loadObject(id, QueryLevel.Mirroring); // 由于查询条目的时候已经锁了数据，所以此处不用再锁定
                }, true);
            } else {
                return DomainBuffer.obtain(table.objectType(), id, () -> {
                    return (IAggregateRoot) table.loadObject(id, QueryLevel.None); // 由于查询条目的时候已经锁了数据，所以此处不用再锁定
                }, false);
            }
        } else {
            Object rootId = entry.get(GeneratedField.RootIdName);
            Object id = entry.get(EntityObject.IdPropertyName);
            String typeKey = (String) entry.get(GeneratedField.TypeKeyName);
            var table = StringUtil.isNullOrEmpty(typeKey) ? _self : DataTableUtil.getDataTable(typeKey);

            // 非聚合根是不能被加入到缓冲区的
            return table.loadObject(rootId, id);
        }
    }

    <T> Iterable<T> query(String expression, Consumer<MapData> fillArg, QueryLevel level) {
        expression = getEntryExpression(expression);
        return getObjectsFromEntries(QueryObjectQB.class, expression, fillArg, level);
    }

    <T> Page<T> query(String expression, int pageIndex, int pageSize, Consumer<MapData> fillArg) {
        expression = getEntryExpression(expression);

        Iterable<T> objects = getObjectsFromEntriesByPage(expression, (data) -> {
            data.put("pageIndex", pageIndex);
            data.put("pageSize", pageSize);
            fillArg.accept(data);
        });
        int count = getCount(expression, fillArg, QueryLevel.None);
        return new Page<T>(pageIndex, pageSize, objects, count);
    }

    int getCount(String expression, Consumer<MapData> fillArg, QueryLevel level) {
        // 获取总数据数
        var param = new MapData();
        fillArg.accept(param);

        var qb = DataSource.getQueryBuilder(QueryCountQB.class);

        var sql = qb.build(QueryDescription.createBy(param, expression, level, _self));

        return DataAccess.current().queryScalarInt(sql, param, level);
    }

//	/**
//	 * 
//	 * 执行一个持久层命令，持久层命令的意义在于可以多数据库支持
//	 * 
//	 * 而仓储里直接写算法，则会与某个特定的数据库绑定，取舍看场景
//	 * 
//	 * 如果需要返回结果，那么写入在items里
//	 * 
//	 * @param expression 实现方可以通过表达式来识别执行什么命令
//	 * @param fillArg
//	 * @param level
//	 */
//	void execute(String expression, Consumer<MapData> fillArg, QueryLevel level,
//			Consumer<Map<String, Object>> fillItems) {
//
//		var param = new MapData();
//		fillArg.accept(param);
//
//		var qb = DataSource.getQueryBuilder(QueryCommandQB.class);
//
//		var items = new HashMap<String, Object>();
//		items.put("expression", expression);
//		items.put("level", level);
//		if (fillItems != null)
//			fillItems.accept(items);
//
//		var description = new QueryDescription(param, items, _self);
//
//		var sql = qb.build(description);
//
//		DataAccess.getCurrent().execute(sql, param);
//
//		return description;
//	}

    @SuppressWarnings("unchecked")
    private <T> Iterable<T> getObjectsFromEntriesByPage(String expression, Consumer<MapData> fillArg) {

        var datas = executeQueryEntries(QueryPageQB.class, expression, QueryLevel.None, fillArg);

        var list = new ArrayList<T>(Iterables.size(datas));

        for (var data : datas) {
            var obj = getObjectFromEntryByPage(data);
            list.add((T) obj);
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    private <T> Iterable<T> getObjectsFromEntries(Class<? extends IQueryBuilder> qbType, String expression,
                                                  Consumer<MapData> fillArg, QueryLevel level) {

        var datas = executeQueryEntries(QueryObjectQB.class, expression, level, fillArg);

        var list = new ArrayList<T>(Iterables.size(datas));

        for (var data : datas) {
            var obj = getObjectFromEntry(data, level);
            list.add((T) obj);
        }

        return list;
    }

//	#region 获取条目信息

    private MapData executeQueryEntry(IQueryBuilder query, String expression, QueryLevel level,
                                      Consumer<MapData> fillArg) {
        var param = new MapData();
        fillArg.accept(param);

        // 编译表达式获取执行文本
        var description = QueryDescription.createBy(param, expression, level, _self);
        var sql = query.build(description);

        return DataAccess.current().queryRow(sql, param, level);
    }

    private Iterable<MapData> executeQueryEntries(Class<? extends IQueryBuilder> qbType, String expression,
                                                  QueryLevel level, Consumer<MapData> fillArg) {
        var param = new MapData();
        fillArg.accept(param);

        var qb = DataSource.getQueryBuilder(qbType);
        var sql = qb.build(QueryDescription.createBy(param, expression, level, _self));
        return DataAccess.current().queryRows(sql, param, level);
    }

    /// <summary>
    /// 根据表达式获取数据条目
    /// </summary>
    /// <param name="level"></param>
    /// <returns></returns>
    private String getEntryExpression(String expression) {
        return _getFindEntryByExpression.apply(_self).apply(expression);
    }

    /**
     * 获得默认查询的字段
     *
     * @param table
     * @return
     */
    private static String getDefaultQueryFields(DataTable table) {
        StringBuilder fields = new StringBuilder();

        for (var field : table.defaultQueryFields()) {

            StringUtil.appendFormat(fields, "%s,", field.name());
        }

        StringUtil.removeLast(fields);

        return fields.toString();
    }

    private static Function<DataTable, Function<String, String>> _getFindEntryByExpression = LazyIndexer
            .init((table) -> {
                return LazyIndexer.init((expression) -> {
                    SqlDefinition def = SqlDefinition.create(expression);
                    if (def.isCustom())
                        return expression; // 如果是自定义查询，那么直接返回表达式，由程序员自行解析
                    var selects = def.columns().select();
                    var selectCount = Iterables.size(selects);
                    var firstSelect = Iterables.getFirst(selects, null);
                    if (selectCount == 0 || firstSelect == "*") {

                        var fieldsSql = getDefaultQueryFields(table);

                        expression = String.format("%s[select %s]", expression, fieldsSql);
						
                    } else if (selectCount == 1) {
                        var propertyName = firstSelect;

                        if (table.type() == DataTableType.AggregateRoot) {
                            expression = MessageFormat.format(
                                    "{0}[select {1}.{2} as {2},{1}.{3} as {3},{1}.{4} as {4}]", expression,
                                    propertyName, EntityObject.IdPropertyName, GeneratedField.DataVersionName,
                                    GeneratedField.TypeKeyName);
                        } else {
                            expression = MessageFormat.format("{0}[select {1},{2}.{3},{2}.{4},{2}.{5}]", expression,
                                    GeneratedField.RootIdName, propertyName, EntityObject.IdPropertyName,
                                    GeneratedField.DataVersionName, GeneratedField.TypeKeyName);
                        }
                    } else if (selectCount > 1) {
                        throw new IllegalStateException(
                                Language.strings("apros.codeart.ddd", "NotSupportMultipleProperties", expression));
                    }

                    return expression;
                });
            });

//	#endregion

//	region 实际查询，构造完整对象

    /// <summary>
    /// 根据编号从数据库中查询数据
    /// </summary>
    /// <param name="id"></param>
    /// <param name="level"></param>
    /// <returns></returns>
    DomainObject loadObject(Object id, QueryLevel level) {
        var expression = getObjectByIdExpression(_self);

        return executeQueryObject(_self.objectType(), expression, (param) -> {
            param.put(EntityObject.IdPropertyName, id);
        }, level);
    }

    /// <summary>
    /// 根据编号从数据库中查询数据
    /// </summary>
    /// <param name="rootId"></param>
    /// <param name="id"></param>
    /// <returns></returns>
    DomainObject loadObject(Object rootId, Object id) {
        var expression = getObjectByIdExpression(_self);

        return executeQueryObject(_self.objectType(), expression, (param) -> {
            param.put(GeneratedField.RootIdName, rootId);
            param.put(EntityObject.IdPropertyName, id);
        }, QueryLevel.None);
    }

    ///// <summary>
    ///// 根据表达式从数据库中查询数据
    ///// </summary>
    ///// <param name="expression"></param>
    ///// <param name="fillArg"></param>
    ///// <param name="level"></param>
    ///// <returns></returns>
    // private object LoadObject(string expression, Action<DynamicData> fillArg,
    ///// QueryLevel level)
    // {
    // var exp = QueryObject.Create(this, expression, level);
    // return ExecuteQueryObject(this.ImplementOrElementType, exp, fillArg);
    // }

    private DomainObject executeQueryObject(Class<?> objectType, String expression, Consumer<MapData> fillArg,
                                            QueryLevel level) {
        var param = new MapData();
        fillArg.accept(param);

        var qb = DataSource.getQueryBuilder(QueryObjectQB.class);
        var sql = qb.build(QueryDescription.createBy(param, expression, level, _self));

        var data = DataAccess.current().queryRow(sql, param, level);
        return _self.createObject(objectType, data, level);
    }

    /// <summary>
    /// 执行查询对象集合的sql
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="sql"></param>
    /// <param name="fillArg"></param>
    /// <returns></returns>
    // private IEnumerable<T> ExecuteQueryObjects<T>(IQueryBuilder query,
    /// Action<DynamicData> fillArg)
    // {
    // using (var paraTemp = SqlHelper.BorrowData())
    // {
    // var param = paraTemp.Item;
    // fillArg(param);
    // var sql = query.Build(param);

    // using (var temp = SqlHelper.BorrowDatas())
    // {
    // var datas = temp.Item;
    // SqlHelper.Query(this.ConnectionName, sql, param, datas);

    // var tempList = ListPool<T>.Borrow();
    // Symbiosis.Current.Mark(tempList);
    // var list = tempList.Item;

    // var elementType = typeof(T);
    // foreach (var data in datas)
    // {
    // var item = (T)CreateObject(elementType, data);
    // list.Add(item);
    // }
    // return list;
    // }
    // }
    // }

//	#endregion

//	region 得到对象版本号

    /**
     * 根据对象原始数据获取版本号
     *
     * @param originalData
     * @return
     */
    int getDataVersion(MapData originalData) {
        if (_self.type() == DataTableType.AggregateRoot) {
            var id = originalData.get(EntityObject.IdPropertyName);
            return getDataVersion(id);
        } else {
            var rootId = originalData.get(GeneratedField.RootIdName);
            var id = originalData.get(EntityObject.IdPropertyName);
            return getDataVersion(rootId, id);
        }
    }

    /// <summary>
    /// 得到对象的版本号
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>
    int getDataVersion(Object id) {
        return getDataVersionImpl((param) -> {
            param.put(EntityObject.IdPropertyName, id);
        });
    }

    int getDataVersion(Object rootId, Object id) {
        return getDataVersionImpl((param) -> {
            param.put(GeneratedField.RootIdName, rootId);
            param.put(EntityObject.IdPropertyName, id);
        });
    }

    private int getDataVersionImpl(Consumer<MapData> fillArg) {
        var expression = getObjectByIdExpression(_self);

        var exp = DataSource.getQueryBuilder(QueryObjectQB.class);

        // 在领域层主动查看数据编号时，不需要锁；在提交数据时获取数据编号，由于对象已被锁，所以不需要在读取版本号时锁
        int dataVersion = 0;
        var data = executeQueryEntry(exp, expression, QueryLevel.None, fillArg);
        if (data.size() > 0) {
            dataVersion = (int) data.get(GeneratedField.DataVersionName);
        }

        return dataVersion;
    }

    public void checkDataVersion(DomainObject root) {
        var id = DataTableUtil.getObjectId(root);
        if (root.dataVersion() != _self.getDataVersion(id)) {
            throw new DataVersionException(root.getClass(), id);
        }
    }

    public int getAssociated(Object rootId, Object id) {
        var data = new MapData();
        data.put(GeneratedField.RootIdName, rootId);
        data.put(EntityObject.IdPropertyName, id);

        var qb = DataSource.getQueryBuilder(GetAssociatedQB.class);
        var sql = qb.build(new QueryDescription(_self));

        return DataAccess.current().queryScalarInt(sql, data, QueryLevel.None);
    }

//	#endregion

    // internal T QuerySingle<T>(IQueryBuilder compiler, Action<DynamicData>
    // fillArg, QueryLevel level)
    // {
    // return (T)ExecuteQueryObject(this.ImplementOrElementType, compiler, fillArg);
    // }

    // internal IEnumerable<T> Query<T>(IQueryBuilder query, Action<DynamicData>
    // fillArg)
    // {
    // return ExecuteQueryObjects<T>(query, fillArg);
    // }

    // internal Page<T> Query<T>(IQueryBuilder pageQuery, IQueryBuilder countQuery,
    // int pageIndex, int pageSize, Action<DynamicData> fillArg)
    // {
    // IList<T> objects = ExecuteQueryObjects<T>(pageQuery, (data) ->
    // {
    // data.Add("pageIndex", pageIndex);
    // data.Add("pageSize", pageSize);
    // fillArg(data);
    // });
    // int count = GetCount(countQuery, fillArg);
    // return new Page<T>(pageIndex, pageSize, objects, count);
    // }

    private static String getObjectByIdExpression(DataTable table) {
        return _getObjectIdExpression.apply(table);
    }

    private static Function<DataTable, String> _getObjectIdExpression = LazyIndexer.init((table) -> {
        String expression = null;
        if (table.type() == DataTableType.AggregateRoot) {
            expression = MessageFormat.format("{0}=@{0}", EntityObject.IdPropertyName);
        } else {
            expression = MessageFormat.format("{0}=@{0} and {1}=@{1}", GeneratedField.RootIdName,
                    EntityObject.IdPropertyName);
        }

        return expression;

    });
}
