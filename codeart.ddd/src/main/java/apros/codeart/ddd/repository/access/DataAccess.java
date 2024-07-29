package apros.codeart.ddd.repository.access;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.internal.QueryRunner;
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

    /**
     * 执行sql
     *
     * @param sql
     */
    public void execute(String sql) {
        QueryRunner.execute(_conn, sql);
    }

    /**
     * 执行sql
     *
     * @param sql
     */
    public int execute(String sql, MapData param) {
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

    public Object queryScalar(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalar(_conn, sql, params, level);
    }

    public Object queryScalar(String sql, MapData params) {
        return queryScalar(sql, params, QueryLevel.NONE);
    }

    public <T> T queryScalar(Class<T> valueType, String sql, MapData param, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalar(valueType, _conn, sql, param, level);
    }

    public <T> T queryScalar(Class<T> valueType, String sql, MapData param) {
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


    public int queryScalarInt(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarInt(_conn, sql, params, level);
    }

    public long queryScalarLong(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarLong(_conn, sql, params, level);
    }

    public long queryScalarLong(String sql, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarLong(_conn, sql, null, level);
    }

    public long queryScalarLong(String sql) {
        return queryScalarLong(sql, QueryLevel.NONE);
    }

    public UUID queryScalarGuid(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarGuid(_conn, sql, params, level);
    }

    public Iterable<Object> queryScalars(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalars(_conn, sql, params, level);
    }

    public Iterable<Object> queryScalars(String sql, MapData params) {
        return queryScalars(sql, params, QueryLevel.NONE);
    }

    public <T> Iterable<T> queryScalars(Class<T> elementType, String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalars(elementType, _conn, sql, params, level);
    }

    public <T> Iterable<T> queryScalars(Class<T> elementType, String sql, MapData params) {
        return queryScalars(elementType, sql, params, QueryLevel.NONE);
    }

    public int[] queryScalarInts(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryScalarInts(_conn, sql, params, level);
    }

    public DTObject queryDTO(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryDTO(_conn, sql, params, level);
    }

    public Iterable<DTObject> queryDTOs(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryDTOs(_conn, sql, params, level);
    }

    public MapData queryRow(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryRow(_conn, sql, params, level);
    }

    public Iterable<MapData> queryRows(String sql, MapData params, QueryLevel level) {
        openDataContextLock(level);
        return QueryRunner.queryRows(_conn, sql, params, level);
    }

    public Iterable<MapData> queryRows(String sql, MapData params) {
        return queryRows(sql, params, QueryLevel.NONE);
    }

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
            } catch (Exception e) {
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
            } catch (Exception e) {
                throw propagate(e);
            }
        }
    }

}
