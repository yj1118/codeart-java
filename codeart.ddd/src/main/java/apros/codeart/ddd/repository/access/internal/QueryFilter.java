package apros.codeart.ddd.repository.access.internal;

import static apros.codeart.i18n.Language.strings;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

import apros.codeart.ddd.MapData;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;
import apros.codeart.util.TimeUtil;

final class QueryFilter {

    private QueryFilter() {
    }

    static interface IQueryFilter {
        void extract(ResultSet rs) throws SQLException;
    }

    public static class Scalar implements IQueryFilter {

        private Object _result;

        public Object result() {
            return _result;
        }

        public Scalar() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            if (rs.next()) {
                _result = rs.getObject(1);
            }
        }

    }

    public static class ScalarInt implements IQueryFilter {

        private int _result;

        public int result() {
            return _result;
        }

        public ScalarInt() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            if (rs.next()) {
                _result = rs.getInt(1);
            }
        }
    }

    public static class ScalarLong implements IQueryFilter {

        private long _result;

        public long result() {
            return _result;
        }

        public ScalarLong() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            if (rs.next()) {
                _result = rs.getLong(1);
            }
        }
    }

    public static class ScalarGuid implements IQueryFilter {

        private UUID _result;

        public UUID result() {
            return _result;
        }

        public ScalarGuid() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            if (rs.next()) {
                _result = UUID.fromString(rs.getString(1));
            }
        }
    }

    public static class Scalars<T> implements IQueryFilter {

        private Iterable<T> _result;

        public Iterable<T> result() {
            return _result;
        }

        public Scalars() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public void extract(ResultSet rs) throws SQLException {
            int length = getCount(rs);

            if (length == 0)
                _result = ListUtil.empty();
            else {
                var items = new ArrayList<T>(length);
                while (rs.next()) {
                    items.add((T) rs.getObject(1));
                }
                _result = items;
            }
        }

    }

    public static class ScalarInts implements IQueryFilter {

        private int[] _result;

        public int[] result() {
            return _result;
        }

        public ScalarInts() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            int length = getCount(rs);

            if (length == 0)
                _result = ListUtil.emptyInts();
            else {
                _result = new int[length];
                int index = 0;
                while (rs.next()) {
                    _result[index] = rs.getInt(1);
                    index++;
                }
            }
        }
    }

    public static class ScalarLongs implements IQueryFilter {

        private long[] _result;

        public long[] result() {
            return _result;
        }

        public ScalarLongs() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            int length = getCount(rs);

            if (length == 0)
                _result = ListUtil.emptyLongs();
            else {
                _result = new long[length];
                int index = 0;
                while (rs.next()) {
                    _result[index] = rs.getLong(1);  //注意，传出的集合是从1开始读取数据
                    index++;
                }
            }
        }
    }

    public static class ScalarGUIDs implements IQueryFilter {

        private UUID[] _result;

        public UUID[] result() {
            return _result;
        }

        public ScalarGUIDs() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            int length = getCount(rs);

            if (length == 0)
                _result = ListUtil.emptyGUIDs();
            else {
                _result = new UUID[length];
                int index = 0;
                while (rs.next()) {
                    _result[index] = UUID.fromString(rs.getString(1));  //注意，传出的集合是从1开始读取数据
                    index++;
                }
            }
        }
    }

    public static class Row implements IQueryFilter {

        private MapData _result;

        public MapData result() {
            return _result;
        }

        public Row() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            _result = new MapData();
            fillSingleData(rs, _result);
        }
    }

    public static class Rows implements IQueryFilter {

        private Iterable<MapData> _result;

        public Iterable<MapData> result() {
            return _result;
        }

        public Rows() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            var rows = new ArrayList<MapData>();
            fillMultipleData(rs, rows);
            _result = rows;
        }

    }

    public static class DTO implements IQueryFilter {

        private DTObject _result;

        public DTObject result() {
            return _result;
        }

        public DTO() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            _result = toDTO(rs);
        }
    }

    public static class DTOs implements IQueryFilter {

        private Iterable<DTObject> _result;

        public Iterable<DTObject> result() {
            return _result;
        }

        public DTOs() {
        }

        @Override
        public void extract(ResultSet rs) throws SQLException {
            _result = toDTOs(rs);
        }
    }

    private static int getCount(ResultSet rs) throws SQLException {
        rs.last(); // 移动到最后一行
        int count = rs.getRow(); // 获取行号，即总行数

        rs.beforeFirst(); // 如果需要，可以将游标移回初始位置继续处理数据

        return count;
    }

    private static void loadRowData(ResultSet rs, MapData data) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int fieldCount = rsmd.getColumnCount();

        for (var i = 1; i <= fieldCount; i++) { // 注意，是从 1 开始的下标
            var value = rs.getObject(i);
            if (rs.wasNull()) {
                // 过滤空数据，只要是空数据，不予加入到结果集中，这对由于引用了外部根，
                // 外部内聚根被删除了导致的情况很有帮助，而且过滤空数据也符合领域驱动empty的原则，
                // 因此数据直接过滤
                continue;
            }

            String name = rsmd.getColumnName(i);
            data.put(name, value);
        }
    }

    private static void fillSingleData(ResultSet rs, MapData data) throws SQLException {
        if (rs.next()) {
            loadRowData(rs, data);
        }
    }

    private static void fillMultipleData(ResultSet rs, ArrayList<MapData> datas) throws SQLException {

        while (rs.next()) {
            var data = new MapData();
            loadRowData(rs, data);
            datas.add(data);
        }
    }

    private static DTObject toDTO(ResultSet rs) throws SQLException {
        if (!rs.next())
            return DTObject.Empty;

        DTObject dto = DTObject.editable();
        fillDTO(rs, dto);
        return dto;
    }

    private static Iterable<DTObject> toDTOs(ResultSet rs) throws SQLException {
        int length = getCount(rs);
        if (length == 0)
            return ListUtil.empty();

        ArrayList<DTObject> dtos = new ArrayList<DTObject>(length);

        while (rs.next()) {
            DTObject dto = DTObject.editable();
            fillDTO(rs, dto);
            dtos.add(dto);
        }

        return dtos;
    }

    private static void fillDTO(ResultSet rs, DTObject dto) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int fieldCount = rsmd.getColumnCount();

        for (var i = 1; i <= fieldCount; i++) { // 注意，是从 1 开始的下标
            var type = rsmd.getColumnType(i);
            switch (type) {

                case Types.BIT:
                case Types.BOOLEAN: {
                    var value = rs.getBoolean(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setBoolean(name, value);
                    break;
                }
                case Types.VARCHAR:
                case Types.CHAR:
                case Types.LONGVARCHAR:
                case Types.NVARCHAR:
                case Types.NCHAR:
                case Types.LONGNVARCHAR: {
                    var value = rs.getString(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setString(name, value);
                    break;
                }
                case Types.TINYINT: {
                    var value = rs.getByte(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setByte(name, value);
                    break;
                }
                case Types.SMALLINT: {
                    var value = rs.getShort(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setShort(name, value);
                    break;
                }
                case Types.INTEGER: {
                    var value = rs.getInt(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setInt(name, value);
                    break;
                }
                case Types.BIGINT: {
                    var value = rs.getLong(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setLong(name, value);
                    break;
                }
                case Types.FLOAT:
                case Types.DOUBLE: {
                    var value = rs.getDouble(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setDouble(name, value);
                    break;
                }
                case Types.REAL: {
                    var value = rs.getFloat(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setFloat(name, value);
                    break;
                }
                case Types.DATE: {
                    var value = rs.getDate(i);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setLocalDateTime(name, TimeUtil.toLocalDateTime(value));
                    break;
                }
                case Types.TIMESTAMP: {

                    String columnTypeName = rsmd.getColumnTypeName(i);

                    if ("timestamptz".equalsIgnoreCase(columnTypeName) ||                   //Postgresql
                            "timestamp with time zone".equalsIgnoreCase(columnTypeName) ||  // Oracle
                            "datetimeoffset".equalsIgnoreCase(columnTypeName)) {            // SQL Server
                        var value = rs.getObject(i, OffsetDateTime.class);
                        if (rs.wasNull())
                            break;
                        String name = rsmd.getColumnName(i);
                        dto.setZonedDateTime(name, value.toZonedDateTime());
                        break;
                    }

                    var value = rs.getObject(i, LocalDateTime.class);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setLocalDateTime(name, value);
                    break;
                }
                case Types.TIMESTAMP_WITH_TIMEZONE: {
                    var value = rs.getObject(i, OffsetDateTime.class);
                    if (rs.wasNull())
                        break;
                    String name = rsmd.getColumnName(i);
                    dto.setZonedDateTime(name, value.toZonedDateTime());
                    break;
                }
                case Types.OTHER: { // PostgreSQL JSONB 类型
                    String columnTypeName = rsmd.getColumnTypeName(i);

                    if ("uuid".equalsIgnoreCase(columnTypeName)) {
                        String name = rsmd.getColumnName(i);
                        String uuidValue = rs.getString(i);
                        var uuid = UUID.fromString(uuidValue);
                        dto.setValue(name, uuid);
                    } else if ("jsonb".equalsIgnoreCase(columnTypeName) || "json".equalsIgnoreCase(columnTypeName)) {
                        String jsonbValue = rs.getString(i);
                        if (rs.wasNull())
                            break;

                        // 至于是用readonly 还是editable，待定
                        var value = DTObject.readonly(jsonbValue);
                        String name = rsmd.getColumnName(i);
                        dto.setObject(name, value);

                    } else {
                        throw new IllegalStateException("Unsupported Types.OTHER column: " + columnTypeName);
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException(strings("apros.codeart.ddd", "TypeMismatch"));
                }
            }

        }

    }

}
