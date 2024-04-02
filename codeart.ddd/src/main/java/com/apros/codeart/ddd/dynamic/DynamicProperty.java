package com.apros.codeart.ddd.dynamic;

import java.time.LocalDateTime;
import java.util.UUID;

import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.metadata.ObjectMeta;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.TypeUtil;

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
		switch (typeName.toLowerCase()) {
		case "char":
			return char.class;
		case "bool":
		case "boolean":
			return boolean.class;
		case "byte":
			return byte.class;
		case "datetime":
			return LocalDateTime.class;
		case "double":
			return double.class;
		case "short":
			return short.class;
		case "int":
			return int.class;
		case "single":
		case "long":
			return long.class;
		case "float":
			return float.class;
		case "ascii":
		case "string":
			return String.class;
		case "guid":
			return UUID.class;
		default:
			return null;
		}
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
