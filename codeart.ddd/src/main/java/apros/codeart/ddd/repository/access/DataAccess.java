package apros.codeart.ddd.repository.access;

import java.sql.Connection;
import java.util.UUID;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.internal.QueryRunner;
import apros.codeart.dto.DTObject;

/**
 * 所有查询都要通过该对象，由该对象传递锁定级别给DataContext
 */
public final class DataAccess {

    private final Connection _conn;

    DataAccess(Connection conn) {
        _conn = conn;
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

    public Object queryScalar(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalar(_conn, sql, params, level);
    }

    public Object queryScalar(String sql, MapData params) {
        return queryScalar(sql, params, QueryLevel.NONE);
    }

    public <T> T queryScalar(Class<T> valueType, String sql, MapData param, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalar(valueType, _conn, sql, param, level);
    }

    public <T> T queryScalar(Class<T> valueType, String sql, MapData param) {
        return queryScalar(valueType, sql, param, QueryLevel.NONE);
    }

    public int queryScalarInt(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalarInt(_conn, sql, params, level);
    }

    public long queryScalarLong(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalarLong(_conn, sql, params, level);
    }

    public long queryScalarLong(String sql, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalarLong(_conn, sql, null, level);
    }

    public long queryScalarLong(String sql) {
        return queryScalarLong(sql, QueryLevel.NONE);
    }

    public UUID queryScalarGuid(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalarGuid(_conn, sql, params, level);
    }

    public Iterable<Object> queryScalars(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalars(_conn, sql, params, level);
    }

    public Iterable<Object> queryScalars(String sql, MapData params) {
        return queryScalars(sql, params, QueryLevel.NONE);
    }

    public <T> Iterable<T> queryScalars(Class<T> elementType, String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalars(elementType, _conn, sql, params, level);
    }

    public <T> Iterable<T> queryScalars(Class<T> elementType, String sql, MapData params) {
        return queryScalars(elementType, sql, params, QueryLevel.NONE);
    }

    public int[] queryScalarInts(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryScalarInts(_conn, sql, params, level);
    }

    public DTObject queryDTO(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryDTO(_conn, sql, params, level);
    }

    public Iterable<DTObject> queryDTOs(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryDTOs(_conn, sql, params, level);
    }

    public MapData queryRow(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
        return QueryRunner.queryRow(_conn, sql, params, level);
    }

    public Iterable<MapData> queryRows(String sql, MapData params, QueryLevel level) {
        DataContext.getCurrent().openLock(level);
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

}
