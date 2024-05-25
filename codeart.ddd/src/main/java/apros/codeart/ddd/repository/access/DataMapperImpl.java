package apros.codeart.ddd.repository.access;

import static apros.codeart.i18n.Language.strings;

import java.util.ArrayList;

import com.google.common.collect.Iterables;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.access.internal.AccessUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public class DataMapperImpl implements IDataMapper {

	public DataMapperImpl() {

	}

	// public virtual void FillInsertData(DomainObject obj, DynamicData data,
	// DataTable table) { }

	public void onPreInsert(DomainObject obj, DataTable table) {
	}

	public void onInserted(DomainObject obj, DataTable table) {
	}

	// public virtual void FillUpdateData(DomainObject obj, DynamicData data,
	// DataTable table) { }

	public void onPreUpdate(DomainObject obj, DataTable table) {
	}

	public void onUpdated(DomainObject obj, DataTable table) {
	}

	public void onPreDelete(DomainObject obj, DataTable table) {
	}

	public void onDeleted(DomainObject obj, DataTable table) {
	}

//	#region 获得类型对应的数据字段

	public Iterable<IDataField> getObjectFields(ObjectMeta meta) {
		var fields = getObjectFieldsByGenerate(meta.objectType());
		// 附加字段
		var attachedFields = getAttachFields(meta);
		ListUtil.addRange(fields, mapFields(meta.objectType(), attachedFields));

		fields.trimToSize();
		return fields;
	}

	private Iterable<IDataField> mapFields(Class<?> objectType, Iterable<DbField> attachedFields) {
		ArrayList<IDataField> fields = new ArrayList<IDataField>(Iterables.size(attachedFields));
		for (var attachedField : attachedFields) {
			var stringField = TypeUtil.as(attachedField, StringField.class);
			if (stringField != null) {
				var field = GeneratedField.createString(objectType, stringField.name(), stringField.maxLength(),
						stringField.ascii());
				field.isAdditional(true);
				fields.add(field);
			} else {
				var field = GeneratedField.create(attachedField.name(), attachedField.valueType(), objectType);
				field.isAdditional(true);
				fields.add(field);
			}
		}
		return fields;
	}

	protected Iterable<DbField> getAttachFields(ObjectMeta meta) {
		// 预留未来使用，这里有可能要对继承类做支持
		return ListUtil.empty();
	}

	private ArrayList<IDataField> getObjectFieldsByGenerate(Class<?> objectType) {
		var domainProperties = PropertyMeta.getProperties(objectType);
		var fields = getFields(domainProperties);

		fields.add(GeneratedField.createTypeKey(objectType));
		fields.add(GeneratedField.createDataVersion(objectType));
		return fields;
	}

	private ArrayList<IDataField> getFields(Iterable<PropertyMeta> domainProperties) {
		ArrayList<IDataField> fields = new ArrayList<IDataField>(Iterables.size(domainProperties));
		for (var domainProperty : domainProperties) {
			IDataField field = getField(domainProperty);
			if (field != null)
				fields.add(field);
		}
		return fields;
	}

	private IDataField getField(PropertyMeta meta) {
		if (meta.isCollection()) {
			return getListField(meta);
		}

		switch (meta.category()) {
		case DomainPropertyCategory.ValueObject: {
			return new ValueObjectField(meta);
		}
		case DomainPropertyCategory.AggregateRoot: {
			// 引用了根对象
			return new AggregateRootField(meta);
		}
		case DomainPropertyCategory.EntityObject: {
			// 引用了内部实体对象
			return new EntityObjectField(meta);
		}
		case DomainPropertyCategory.Primitive: {
			// 普通的值数据
			return AccessUtil.isId(meta) ? new ValueField(meta, DbFieldType.PrimaryKey) : new ValueField(meta);
		}
		default:
			break;
		}
		return null;
	}

	/**
	 * 获取集合类型的数据字段
	 * 
	 * @param attribute
	 * @return
	 */
	private IDataField getListField(PropertyMeta meta) {
		var elementType = meta.monotype();

		if (ObjectMeta.isValueObject(elementType)) {
			// 值对象
			return new ValueObjectListField(meta);
		} else if (ObjectMeta.isAggregateRoot(elementType)) {
			// 引用了根对象
			return new AggregateRootListField(meta);
		} else if (ObjectMeta.isEntityObject(elementType)) {
			// 引用了内部实体对象
			return new EntityObjectListField(meta);
		} else if (TypeUtil.isCollection(elementType)) {
			throw new IllegalStateException(strings("apros.codeart.ddd", "NestedCollection"));
		} else {
			// 值集合
			return new ValueListField(meta);
		}
	}

//	#endregion

	public String build(IQueryBuilder builder, QueryDescription descripton) {
		return StringUtil.empty();
	}

//	#region 静态成员

	public static final DataMapperImpl Instance = new DataMapperImpl();

	/**
	 * 关键字段的定义，只要通过关键字段的查询，ORM引擎就可以加载完整的根对象，该属性用于自定义查询的select语句中
	 */
	public static final String KeyFields;

	static {
		KeyFields = String.format("%s,%s,%s", EntityObject.IdPropertyName, GeneratedField.DataVersionName,
				GeneratedField.TypeKeyName);
	}

//	#endregion

}
