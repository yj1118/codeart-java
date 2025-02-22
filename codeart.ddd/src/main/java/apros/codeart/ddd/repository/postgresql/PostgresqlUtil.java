package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.DbType;
import apros.codeart.i18n.Language;

import java.time.OffsetDateTime;

public final class PostgresqlUtil {

    private PostgresqlUtil() {
    }

    public static String getSqlDbTypeString(DbType dbType) {
        return switch (dbType) {
            case DbType.AnsiString, DbType.String -> "varchar";
            case DbType.Byte, DbType.Int16 -> "smallint"; // postgresql里没有8位存储，至少存16位
            case DbType.Boolean -> "boolean";
            case DbType.LocalDateTime -> "timestamp";
            case OffsetDateTime, DbType.ZonedDateTime -> "timestamptz";
            case DbType.Float -> "real";
            case DbType.Double -> "double precision";
            case DbType.Guid -> "uuid";
            case DbType.Int32 -> "integer";
            case DbType.Int64 -> "bigint";
            case DbType.BigDecimal -> "NUMERIC";
            default -> throw new IllegalStateException(
                    Language.strings("apros.codeart.ddd", "UnsupportedFieldType", dbType.toString()));
        };
    }

}
