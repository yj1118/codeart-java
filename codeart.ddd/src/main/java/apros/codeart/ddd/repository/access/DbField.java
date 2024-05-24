package apros.codeart.ddd.repository.access;

import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeCode;
import apros.codeart.runtime.TypeUtil;

public abstract class DbField {

	private String _name;

	public String name() {
		return _name;
	}

	public abstract Class<?> valueType();

	public DbField(String name) {
		_name = name;
	}

	/**
	 * 根据类型创造数据库字段，字符串类型请直接构造，不要调用该方法
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public static DbField create(String name, Class<?> type) {

		switch (TypeUtil.getTypeCode(type)) {
		case TypeCode.Boolean:
			return new BooleanField(name);
		case TypeCode.Byte:
			return new ByteField(name);
		case TypeCode.DateTime:
			return new DataTimeField(name);
		case TypeCode.Float:
			return new FloatField(name);
		case TypeCode.Double:
			return new DoubleField(name);
		case TypeCode.Short:
			return new ShortField(name);
		case TypeCode.Int:
			return new IntField(name);
		case TypeCode.Long:
			return new LongField(name);
		case TypeCode.Guid: {
			return new GuidField(name);
		}
		default:
			break;
		}
		throw new IllegalStateException(Language.strings("apros.codeart.ddd", "UnsupportedFieldType", type.getSimpleName()));
	}
}
