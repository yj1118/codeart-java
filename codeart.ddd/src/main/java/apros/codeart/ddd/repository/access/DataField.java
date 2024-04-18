package apros.codeart.ddd.repository.access;

import java.util.ArrayList;

import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public abstract class DataField implements IDataField {

	private String _name;

	public String name() {
		if (StringUtil.isNullOrEmpty(_name))
			return this.propertyName();
		return _name;
	}

	public void name(String value) {
		_name = value;
	}

	private boolean _isAdditional;

	public boolean isAdditional() {
		return _isAdditional;
	}

	public void isAdditional(boolean value) {
		_isAdditional = value;
	}

	public String propertyName() {
		return this.tip().name();
	}

	public Class<?> propertyType() {
		return this.tip().monotype();
	}

	public Class<?> reflectedType() {
		return this.tip().declaringType();
	}

	public abstract DataFieldType fieldType();

	public abstract boolean isMultiple();

	private DbType _dbType;

	public DbType dbType() {
		return _dbType;
	}

	private ArrayList<DbFieldType> _dbFieldTypes;

	public Iterable<DbFieldType> dbFieldTypes() {
		return _dbFieldTypes;
	}

	public void addDbFieldType(DbFieldType fieldType) {
		if (!_dbFieldTypes.contains(fieldType))
			_dbFieldTypes.add(fieldType);
	}

	public boolean isPrimaryKey() {
		return ListUtil.contains(_dbFieldTypes, (t) -> {
			return t == DbFieldType.PrimaryKey;
		});
	}

	public boolean isClusteredIndex() {
		return ListUtil.contains(_dbFieldTypes, (t) -> {
			return t == DbFieldType.ClusteredIndex;
		});
	}

	public boolean isNonclusteredIndex() {
		return ListUtil.contains(_dbFieldTypes, (t) -> {
			return t == DbFieldType.NonclusteredIndex;
		});
	}

	private PropertyMeta _tip;

	public PropertyMeta tip() {
		return _tip;
	}

	private IDataField _parentMemberField;

	public IDataField parentMemberField() {
		return _parentMemberField;
	}

	public void parentMemberField(IDataField value) {
		_parentMemberField = value;
	}

	private DataTable _table;

	public DataTable table() {
		return _table;
	}

	public void table(DataTable value) {
		_table = value;
	}

	private String _tableName;

	public String tableName() {
		return _tableName;
	}

	public void tableName(String value) {
		_tableName = value;
	}

	private String _masterTableName;

	public String masterTableName() {
		return _masterTableName;
	}

	public void masterTableName(String value) {
		_masterTableName = value;
	}

	public DataField(PropertyMeta tip, DbType dbType, DbFieldType[] dbFieldTypes) {
		_isAdditional = false; // 默认情况下不是附加字段
		_tip = tip;
		_dbType = dbType;
		_dbFieldTypes = ListUtil.map(dbFieldTypes, (t) -> t);
	}

}
