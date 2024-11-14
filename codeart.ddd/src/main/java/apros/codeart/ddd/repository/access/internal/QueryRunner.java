package apros.codeart.ddd.repository.access.internal;

import static apros.codeart.runtime.Util.propagate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataSource;
import apros.codeart.ddd.repository.access.DatabaseType;
import apros.codeart.runtime.EnumUtil;
import apros.codeart.util.HashUtil;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterables;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.repository.access.internal.QueryFilter.IQueryFilter;
import apros.codeart.ddd.repository.access.internal.QueryFilter.Row;
import apros.codeart.ddd.repository.access.internal.QueryFilter.Rows;
import apros.codeart.dto.DTObject;
import apros.codeart.util.LazyIndexer;

import javax.xml.crypto.Data;

public final class QueryRunner {
    private QueryRunner() {

    }

    /**
     * 用于执行不带返回值的sql
     *
     * @param conn
     * @param sql
     */
    public static void execute(Connection conn, String sql) {

        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    /**
     * 用于执行insert,update,delete的语句
     *
     * @param conn
     * @param sql
     * @param param
     * @return
     */
    public static int execute(Connection conn, String sql, MapData param) {

        try (PreparedStatement pstmt = getStatement(conn, sql, param);) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw propagate(e);
        }

    }

    public static Object queryScalar(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.Scalar();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    @SuppressWarnings("unchecked")
    public static <T> T queryScalar(Class<T> valueType, Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.Scalar();
        query(conn, sql, param, filter, level);
        return (T) filter.result();
    }

    public static int queryScalarInt(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.ScalarInt();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static long queryScalarLong(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.ScalarLong();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static UUID queryScalarGuid(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.ScalarGuid();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static Iterable<Object> queryScalars(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.Scalars<Object>();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static <T> Iterable<T> queryScalars(Class<T> elementType, Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.Scalars<T>();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static int[] queryScalarInts(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.ScalarInts();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static long[] queryScalarLongs(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.ScalarLongs();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static DTObject queryDTO(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.DTO();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static Iterable<DTObject> queryDTOs(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new QueryFilter.DTOs();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    /**
     * 多行
     *
     * @param conn
     * @param sql
     * @param param
     * @return
     */
    public static MapData queryRow(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new Row();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    public static Iterable<MapData> queryRows(Connection conn, String sql, MapData param, QueryLevel level) {
        var filter = new Rows();
        query(conn, sql, param, filter, level);
        return filter.result();
    }

    private static void query(Connection conn, String sql, MapData param, IQueryFilter filter, QueryLevel level) {
        tryOpenAppLock(conn, sql, param, level);
        try (PreparedStatement pstmt = getStatement(conn, sql, param); ResultSet rs = pstmt.executeQuery()) {
            filter.extract(rs);
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private static void tryOpenAppLock(Connection conn, String sql, MapData param, QueryLevel level) {
        // 只有hold锁才需要额外的加应用程序锁
        if (!level.equals(QueryLevel.HOLD)) return;

        var type = DataSource.getDatabaseType();
        if (type == DatabaseType.SqlServer) return; //sqlserver不需要应用程序锁就可以实现hold锁


        var hashCode = HashUtil.hash64((hasher) -> {
            hasher.append(sql);
            if (param != null)
                hasher.append(param.entrySet());
        });

        // 目前仅支持postgresql,以后再补充其他数据库,todo
        switch (type) {
            case PostgreSql -> execute(conn, String.format("SELECT pg_advisory_xact_lock(%s)", hashCode));
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }


    private static class QueryAdapter {

        private final String _sql;

        /**
         * 要执行的语句
         *
         * @return
         */
        public String sql() {
            return _sql;
        }

        /**
         * 每个项是一个参数名称，所在数组的序号+1就是他在sql里的参数序号
         */
        private final String[] _positions;

        public QueryAdapter(String sql, String[] positions) {
            _sql = sql;
            _positions = positions;
        }

        /**
         * 将键值对的参数，转换为匹配sql语句的参数数组
         *
         * @param param
         * @return
         */
        public void fillParams(PreparedStatement statement, MapData param) {
            if (param == null)
                return;
            try {

                for (var i = 0; i < _positions.length; i++) {
                    var name = _positions[i];
                    var value = param.get(name);

                    if (value instanceof Enum) {
                        value = (byte) EnumUtil.getValue(value);
                    }

                    var index = i + 1; // sql里的参数是从1开始算的
                    statement.setObject(index, value);
                }

            } catch (SQLException e) {
                throw propagate(e);
            }
        }

        public static QueryAdapter parse(String sql) {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> positions = new ArrayList<>();
            for (int i = 0; i < sql.length(); i++) {
                char ch = sql.charAt(i);
                if (ch == '@') {
                    var name = findName(sql, i);
                    positions.add(name);
                    i += name.length();
                    sb.append("?");
                    continue;
                }
                sb.append(ch);
            }

            return new QueryAdapter(sb.toString(), Iterables.toArray(positions, String.class));
        }

        private static final CharMatcher ALPHANUMERIC_UNDERSCORE = CharMatcher.inRange('A', 'Z')
                .or(CharMatcher.inRange('a', 'z'))
                .or(CharMatcher.inRange('0', '9'))
                .or(CharMatcher.is('_'));

        public static boolean isAlphanumericOrUnderscore(char ch) {
            return ALPHANUMERIC_UNDERSCORE.matches(ch);
        }

        private static String findName(String sql, int index) {
            StringBuilder name = new StringBuilder();
            // index 对应的是@,index+1才是真正的名称的开始
            for (int i = (index + 1); i < sql.length(); i++) {
                char ch = sql.charAt(i);
                if (!isAlphanumericOrUnderscore(ch)) {
                    return name.toString();
                }
                name.append(ch);
            }
            return name.toString();
        }

    }

    private static final Function<String, QueryAdapter> _getAdapter = LazyIndexer.init((sql) -> {
        return QueryAdapter.parse(sql);
    });

    private static PreparedStatement getStatement(Connection conn, String sql, MapData param) throws SQLException {
        var adapter = _getAdapter.apply(sql);
        PreparedStatement pstmt = conn.prepareStatement(adapter.sql(),
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        adapter.fillParams(pstmt, param);
        return pstmt;
    }
}
