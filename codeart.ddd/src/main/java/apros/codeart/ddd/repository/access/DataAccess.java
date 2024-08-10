package apros.codeart.ddd.repository.access;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Page;
import apros.codeart.ddd.repository.access.internal.QueryRunner;
import apros.codeart.ddd.repository.access.internal.SqlNativeAssist;
import apros.codeart.dto.DTObject;

import static apros.codeart.runtime.Util.propagate;

/**
 * 所有查询都要通过该对象，由该对象传递锁定级别给DataContext
 */
public final class DataAccess {

    private final Connection _conn;

    DataAccess(Connection conn) {
        _conn = conn;
    }

    private void openDataContextLock(QueryLevel level) {
        // 也有可能在没有数据上下文的情况下查询，这时候是独立的数据库连接，与上下文无关
        if (DataContext.existCurrent())
            DataContext.getCurrent().openLock(level);
    }


    //region execute

    private static String getNativeSql(String sql, MapData param) {
        var assist = SqlNativeAssist.create(sql);
        return assist.parse(param);
    }


    /**
     * 执行sql
     *
     * @param sql
     */
    public void execute(String sql) {
        var nativeSql = getNativeSql(sql, null);
        QueryRunner.execute(_conn, nativeSql);
    }

    public void nativeExecute(String sql) {
        QueryRunner.execute(_conn, sql);
    }

    /**
     * 执行sql
     *
     * @param sql
     */
    public int execute(String sql, MapData param) {
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.execute(_conn, nativeSql, param);
    }

    public int nativeExecute(String sql, MapData param) {
        return QueryRunner.execute(_conn, sql, param);
    }

    //region execute的适配模式

    public ExecuteAdaptation execute(MapData param) {
        return new ExecuteAdaptation(this, param);
    }

    public ExecuteAdaptation execute() {
        return new ExecuteAdaptation(this, null);
    }

    public static class ExecuteAdaptation {

        private final DataAccess _access;
        private final MapData _param;

        private boolean _matched = false;

        public ExecuteAdaptation(DataAccess access, MapData param) {
            _access = access;
            _param = param;
        }

        public ExecuteAdaptation postgreSql(String sql) {
            return exec(DatabaseType.PostgreSql, sql);
        }

        public ExecuteAdaptation sqlserver(String sql) {
            return exec(DatabaseType.SqlServer, sql);
        }

        public ExecuteAdaptation mysql(String sql) {
            return exec(DatabaseType.MySql, sql);
        }

        public ExecuteAdaptation oracle(String sql) {
            return exec(DatabaseType.Oracle, sql);
        }


        private ExecuteAdaptation exec(DatabaseType dbType, String sql) {
            if (_matched) return this;
            if (DataSource.getDatabaseType() == dbType) {
                _access.execute(sql, _param);
                _matched = true;
            }
            return this;
        }
    }

    //endregion

    //endregion

    //region queryScalar

    public Object queryScalar(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalar(_conn, nativeSql, param, level);
    }

    public Object queryScalar(String sql, MapData params) {
        return queryScalar(sql, params, QueryLevel.NONE);
    }

    public <T> T queryScalar(Class<T> valueType, String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalar(valueType, _conn, nativeSql, param, level);
    }

    public <T> T queryScalar(Class<T> valueType, String sql, MapData param) {
        return queryScalar(valueType, sql, param, QueryLevel.NONE);
    }


    public Object nativeQueryScalar(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalar(_conn, sql, params, level);
    }

    public Object nativeQueryScalar(String sql, MapData params) {
        return queryScalar(sql, params, QueryLevel.NONE);
    }


    public <T> T nativeQueryScalar(Class<T> valueType, String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalar(valueType, _conn, sql, param, level);
    }

    public <T> T nativeQueryScalar(Class<T> valueType, String sql, MapData param) {
        return queryScalar(valueType, sql, param, QueryLevel.NONE);
    }

    //region queryScalar的适配模式

    public <T> QueryScalarAdaptation<T> queryScalar(Class<T> valueType, MapData param, QueryLevel level) {
        return new QueryScalarAdaptation<T>(this, valueType, param, level);
    }

    public <T> QueryScalarAdaptation<T> queryScalar(Class<T> valueType, QueryLevel level) {
        return new QueryScalarAdaptation<T>(this, valueType, null, level);
    }


    public <T> QueryScalarAdaptation<T> queryScalar(Class<T> valueType, MapData param) {
        return new QueryScalarAdaptation<T>(this, valueType, param, QueryLevel.NONE);
    }

    public <T> QueryScalarAdaptation<T> queryScalar(Class<T> valueType) {
        return new QueryScalarAdaptation<T>(this, valueType, null, QueryLevel.NONE);
    }

    public static class QueryScalarAdaptation<T> {

        private T _value;

        public T value() {
            return _value;
        }

        private final DataAccess _access;
        private final Class<T> _valueType;
        private final MapData _param;
        private final QueryLevel _level;

        private boolean _matched = false;

        public QueryScalarAdaptation(DataAccess access, Class<T> valueType, MapData param, QueryLevel level) {
            _access = access;
            _valueType = valueType;
            _param = param;
            _level = level;
        }

        public QueryScalarAdaptation<T> postgreSql(String sql) {
            return exec(DatabaseType.PostgreSql, sql);
        }

        public QueryScalarAdaptation<T> sqlserver(String sql) {
            return exec(DatabaseType.SqlServer, sql);
        }

        public QueryScalarAdaptation<T> mysql(String sql) {
            return exec(DatabaseType.MySql, sql);
        }

        public QueryScalarAdaptation<T> oracle(String sql) {
            return exec(DatabaseType.Oracle, sql);
        }


        private QueryScalarAdaptation<T> exec(DatabaseType dbType, String sql) {
            if (_matched) return this;
            if (DataSource.getDatabaseType() == dbType) {
                _value = _access.queryScalar(_valueType, sql, _param, _level);
                _matched = true;
            }
            return this;
        }


    }

    //endregion

    //endregion

    //region queryScalar(Value)

    public int queryScalarInt(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalarInt(_conn, nativeSql, param, level);
    }

    public int nativeQueryScalarInt(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarInt(_conn, sql, params, level);
    }

    public long queryScalarLong(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalarLong(_conn, nativeSql, param, level);
    }

    public long nativeQueryScalarLong(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarLong(_conn, sql, params, level);
    }

    public long queryScalarLong(String sql, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, null);
        return QueryRunner.queryScalarLong(_conn, nativeSql, null, level);
    }

    public long nativeQueryScalarLong(String sql, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarLong(_conn, sql, null, level);
    }

    public long nativeQueryScalarLong(String sql) {
        return nativeQueryScalarLong(sql, QueryLevel.NONE);
    }

    public UUID queryScalarGuid(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalarGuid(_conn, nativeSql, param, level);
    }

    public UUID nativeQueryScalarGuid(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarGuid(_conn, sql, param, level);
    }

    //endregion

    //region queryScalars

    public Iterable<Object> queryScalars(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalars(_conn, nativeSql, param, level);
    }

    public Iterable<Object> queryScalars(String sql, MapData params) {
        return queryScalars(sql, params, QueryLevel.NONE);
    }

    public <T> Iterable<T> queryScalars(Class<T> elementType, String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalars(elementType, _conn, nativeSql, param, level);
    }

    public <T> Iterable<T> queryScalars(Class<T> elementType, String sql, MapData params) {
        return queryScalars(elementType, sql, params, QueryLevel.NONE);
    }


    public Iterable<Object> nativeQueryScalars(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalars(_conn, sql, params, level);
    }

    public Iterable<Object> nativeQueryScalars(String sql, MapData params) {
        return nativeQueryScalars(sql, params, QueryLevel.NONE);
    }

    public <T> Iterable<T> nativeQueryScalars(Class<T> elementType, String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalars(elementType, _conn, sql, params, level);
    }

    public <T> Iterable<T> nativeQueryScalars(Class<T> elementType, String sql, MapData params) {
        return queryScalars(elementType, sql, params, QueryLevel.NONE);
    }


    //endregion

    //#region queryScalar(Values)

    public int[] queryScalarInts(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryScalarInts(_conn, nativeSql, param, level);
    }

    public int[] nativeQueryScalarInts(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarInts(_conn, sql, params, level);
    }

    //endregion

    public DTObject queryDTO(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryDTO(_conn, sql, params, level);
    }

    public DTObject nativeQueryDTO(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryDTO(_conn, nativeSql, param, level);
    }

    public Iterable<DTObject> queryDTOs(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryDTOs(_conn, sql, params, level);
    }

    public Iterable<DTObject> nativeQueryDTOs(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryDTOs(_conn, nativeSql, param, level);
    }

    public MapData queryRow(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryRow(_conn, sql, params, level);
    }

    public MapData nativeQueryRow(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryRow(_conn, sql, param, level);
    }

    //region queryRows

    public Iterable<MapData> queryRows(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryRows(_conn, sql, params, level);
    }

    public Iterable<MapData> queryRows(String sql, MapData params) {
        return queryRows(sql, params, QueryLevel.NONE);
    }

    public Iterable<MapData> nativeQueryRows(String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        var nativeSql = getNativeSql(sql, param);
        return QueryRunner.queryRows(_conn, nativeSql, param, level);
    }

    public Iterable<MapData> nativeQueryRows(String sql, MapData param) {
        return queryRows(sql, param, QueryLevel.NONE);
    }

    //endregion

    //region queryPage

    public Page<DTObject> queryPage(QueryPageCode code, int pageIndex, int pageSize, MapData param) {

        var compiler = DataSource.getAgent().getPageCompiler();
        var pageSql = compiler.buildPage(code, pageIndex, pageSize);
        var nativePageSql = getNativeSql(pageSql, param);
        var rows = QueryRunner.queryDTOs(_conn, nativePageSql, param, QueryLevel.NONE);

        var countSql = compiler.buildCount(code);
        var nativeCountSql = getNativeSql(countSql, param);
        var count = QueryRunner.queryScalarInt(_conn, nativeCountSql, param, QueryLevel.NONE);

        return new Page<>(pageIndex, pageSize, rows, count);
    }

    public Page<DTObject> nativeQueryPage(QueryPageCode code, int pageIndex, int pageSize, MapData param) {

        var compiler = DataSource.getAgent().getPageCompiler();
        var pageSql = compiler.buildPage(code, pageIndex, pageSize);
        var rows = QueryRunner.queryDTOs(_conn, pageSql, param, QueryLevel.NONE);

        var countSql = compiler.buildPage(code, pageIndex, pageSize);
        var count = QueryRunner.queryScalarInt(_conn, countSql, param, QueryLevel.NONE);

        return new Page<>(pageIndex, pageSize, rows, count);
    }

    //region queryPage的适配模式

    public QueryPageAdaptation queryPage(int pageIndex, int pageSize, MapData param) {
        return new QueryPageAdaptation(this, pageIndex, pageSize, param);
    }

    public static class QueryPageAdaptation {

        private Page<DTObject> _value;

        public Page<DTObject> value() {
            return _value;
        }

        private final DataAccess _access;
        private final int _pageIndex;
        private final int _pageSize;
        private final MapData _param;

        private boolean _matched = false;

        public QueryPageAdaptation(DataAccess access, int pageIndex, int pageSize, MapData param) {
            _access = access;
            _pageIndex = pageIndex;
            _pageSize = pageSize;
            _param = param;
        }

        public QueryPageAdaptation postgreSql(QueryPageCode code) {
            return exec(DatabaseType.PostgreSql, code);
        }

        public QueryPageAdaptation sqlserver(QueryPageCode code) {
            return exec(DatabaseType.SqlServer, code);
        }

        public QueryPageAdaptation mysql(QueryPageCode code) {
            return exec(DatabaseType.MySql, code);
        }

        public QueryPageAdaptation oracle(QueryPageCode code) {
            return exec(DatabaseType.Oracle, code);
        }

        private QueryPageAdaptation exec(DatabaseType dbType, QueryPageCode code) {
            if (_matched) return this;
            if (DataSource.getDatabaseType() == dbType) {
                _value = _access.queryPage(code, _pageIndex, _pageSize, _param);
                _matched = true;
            }
            return this;
        }
    }

    //endregion


    //endregion

    public static DataAccess current() {
        if (!DataContext.existCurrent())
            return null;
        return DataContext.getCurrent().connection().access();
    }

    public static void using(Consumer<DataAccess> action) {

        var current = DataAccess.current();
        if (current != null) {
            action.accept(current);
        } else {
            try (var conn = DataSource.getConnection()) {
                var access = new DataAccess(conn);
                action.accept(access);
            } catch (Throwable e) {
                throw propagate(e);
            }
        }
    }

    public static <T> T using(Function<DataAccess, T> action) {

        var current = DataAccess.current();
        if (current != null) {
            return action.apply(current);
        } else {
            try (var conn = DataSource.getConnection()) {
                var access = new DataAccess(conn);
                return action.apply(access);
            } catch (Throwable e) {
                throw propagate(e);
            }
        }
    }

}
