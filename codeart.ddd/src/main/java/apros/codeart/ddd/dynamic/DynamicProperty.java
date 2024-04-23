package apros.codeart.ddd.dynamic;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.dynamic.internal.ListEntry;
import apros.codeart.ddd.dynamic.internal.ObjectEntry;
import apros.codeart.ddd.dynamic.internal.TypeDefine;
import apros.codeart.ddd.dynamic.internal.TypeEntry;
import apros.codeart.ddd.dynamic.internal.ValueEntry;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.PrimitiveUtil;

public final class DynamicProperty {

	private DynamicProperty() {
	}

	/**
	 * 注入动态信息
	 * 
	 * @param declaringType
	 * @param objectMetaCode
	 */
	public static void register(Class<?> declaringType, String objectMetaCode) {
		var define = TypeDefine.getMetadata(objectMetaCode);
		registerProperies(declaringType, define);
	}

	public static void register(Class<?> declaringType, DTObject objectMeta) {
		register(declaringType, objectMeta.getCode());
	}

	/**
	 * 
	 * 注册领域属性
	 * 
	 * @param declaringType
	 * @param define
	 */
	private static void registerProperies(Class<?> declaringType, TypeDefine define) {
		for (var entry : define.getEntries()) {
			var valueEntry = TypeUtil.as(entry, ValueEntry.class);
			if (valueEntry != null) {
				registerValueProperty(declaringType, valueEntry);
				continue;
			}

			var objectEntry = TypeUtil.as(entry, ObjectEntry.class);
			if (objectEntry != null) {
				registerObjectProperty(declaringType, objectEntry);
				continue;
			}

			var listEntry = TypeUtil.as(entry, ListEntry.class);
			if (listEntry != null) {
				registerCollectionProperty(declaringType, listEntry);
				continue;
			}
		}
	}

//	#region 基元值的属性

	private static void registerValueProperty(Class<?> declaringType, ValueEntry entry) {
		var propertyName = entry.getName();
		var propertyType = getValueType(entry);

		// 这里要分析更多的定义，比如默认值，属性的验证设置等，都可以通过配置（不是写死的，是有扩展算法的） todo
		DomainProperty.register(propertyName, declaringType, propertyType);
	}

	private static Class<?> getValueType(ValueEntry entry) {
		var type = getPrimitiveType(entry.getTypeName());
		if (type == null) {
			throw new IllegalArgumentException(
					Language.strings("codeart.ddd", "UnrecognizedType", entry.getTypeName()));
		}
		return type;
	}

	public static Class<?> getPrimitiveType(String typeName) {

		if (typeName.equalsIgnoreCase("ascii"))
			return String.class;

		return PrimitiveUtil.getType(typeName);
	}

//	#endregion

//	#region 对象的属性

	private static void registerObjectProperty(Class<?> declaringType, ObjectEntry entry) {
		var propertyName = entry.getName();
		var meta = getObjectMeta(entry);
		var propertyType = meta.objectType();

		// 这里要分析更多的定义，比如默认值，属性的验证设置等，都可以通过配置（不是写死的，是有扩展算法的） todo
		DomainProperty.register(propertyName, propertyType, declaringType);
	}

	private static ObjectMeta getObjectMeta(ObjectEntry entry) {
		return ObjectMetaLoader.get(entry.getTypeName());
	}

//	#endregion

	private static void registerCollectionProperty(Class<?> declaringType, ListEntry entry) {
		var itemEntry = entry.getItemEntry();
		var elementType = getObjectType(declaringType, itemEntry);

		String propertyName = entry.getName();

		DomainProperty.registerCollection(propertyName, elementType, declaringType);
	}

	private static Class<?> getObjectType(Class<?> declaringType, TypeEntry entry) {
		var valueEntry = TypeUtil.as(entry, ValueEntry.class);
		if (valueEntry != null) {
			return getValueType(valueEntry);
		}

		var objectEntry = TypeUtil.as(entry, ObjectEntry.class);
		if (objectEntry != null) {
			return getObjectMeta(objectEntry).objectType();
		}

		throw new DomainDrivenException(Language.strings("TypeDefineFindTypeError", entry.getMetadataCode()));
	}

}
