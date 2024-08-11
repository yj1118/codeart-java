package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.DbType;
import apros.codeart.i18n.Language;

public final class PostgresqlUtil {

    private PostgresqlUtil() {
    }

    public static String getSqlDbTypeString(DbType dbType) {
        return switch (dbType) {
            case DbType.AnsiString, DbType.String -> "varchar";
            case DbType.Byte, DbType.Int16 -> "smallint";
            case DbType.Boolean -> "boolean";
            case DbType.LocalDateTime -> "timestamp";
            case DbType.ZonedDateTime -> "timestamptz";
            case DbType.Float -> "real";
            case DbType.Double -> "double precision";
            case DbType.Guid -> "uuid";
            case DbType.Int32 -> "integer";
            case DbType.Int64 -> "bigint";
            default -> throw new IllegalStateException(
                    Language.strings("apros.codeart.ddd", "UnsupportedFieldType", dbType.toString()));
        };
    }

}
