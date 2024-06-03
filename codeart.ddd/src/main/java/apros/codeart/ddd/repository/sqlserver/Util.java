package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.repository.access.DbType;
import apros.codeart.i18n.Language;

public final class Util {

	private Util() {
	}

	public static String getSqlDbTypeString(DbType dbType) {
		switch (dbType) {
		case DbType.AnsiString:
			return "varchar";
		case DbType.Byte:
			return "tinyint";
		case DbType.Boolean:
			return "BIT";
		case DbType.LocalDateTime:
			return "datetime2";
		case DbType.ZonedDateTime:
			return "datetimeoffset";
		case DbType.Float:
			return "real";
		case DbType.Double:
			return "float";
		case DbType.Guid:
			return "uniqueidentifier";
		case DbType.Int16:
			return "smallint";
		case DbType.Int32:
			return "int";
		case DbType.Int64:
			return "bigint";
		case DbType.String:
			return "nvarchar";
		default:
			break;
		}
		throw new IllegalStateException(
				Language.strings("apros.codeart.ddd", "UnsupportedFieldType", dbType.toString()));
	}

//	public static SqlDbType etSqlDbType(DbType dbType) {
//		return _getSqlDbType(dbType);
//	}
//
//	private static Func<DbType, SqlDbType> _getSqlDbType = LazyIndexer.Init<DbType, SqlDbType>((dbType)=>
//	{
//		SqlParameter p = new SqlParameter();
//		p.DbType = dbType;
//		return p.SqlDbType;
//	});
//
//	public static string GetSqlDbTypeString(Type type) {
//		return _getSqlDbTypeStringByNetType(type);
//	}
//
//	private static Func<Type, string> _getSqlDbTypeStringByNetType = LazyIndexer.Init<Type, string>((type)=>
//	{
//		var sqlDbType = GetSqlDbType(type);
//		return sqlDbType.ToString().ToLower();
//	});
//
//	private static SqlDbType GetSqlDbType(Type type) {
//		switch (Type.GetTypeCode(type)) {
//		case TypeCode.Boolean:
//			return SqlDbType.Bit;
//		case TypeCode.Byte:
//			return SqlDbType.TinyInt;
//		case TypeCode.DateTime:
//			return SqlDbType.DateTime;
//		case TypeCode.Decimal:
//			return SqlDbType.Decimal;
//		case TypeCode.Double:
//			return SqlDbType.Float;
//		case TypeCode.Int16:
//			return SqlDbType.SmallInt;
//		case TypeCode.Int32:
//			return SqlDbType.Int;
//		case TypeCode.Int64:
//			return SqlDbType.BigInt;
//		case TypeCode.SByte:
//			return SqlDbType.TinyInt;
//		case TypeCode.Single:
//			return SqlDbType.Real;
//		case TypeCode.String:
//			return SqlDbType.NVarChar;
//		case TypeCode.UInt16:
//			return SqlDbType.SmallInt;
//		case TypeCode.UInt32:
//			return SqlDbType.Int;
//		case TypeCode.UInt64:
//			return SqlDbType.BigInt;
//		case TypeCode.Object: {
//			if (type == typeof(Guid))
//				return SqlDbType.UniqueIdentifier;
//			break;
//		}
//		}
//		throw new DataAccessException(string.Format(Strings.NotSupportDbType, type.Name));
//	}

}
