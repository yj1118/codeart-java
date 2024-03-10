package com.apros.codeart.dto.serialization;

import com.apros.codeart.dto.DTObject;

public class DTObjectMapper {
	private DTObjectMapper() {
	}

	/**
	 * 根据架构代码，将dto的数据创建到新实例<paramref name="instanceType"/>中
	 * 
	 * @param instanceType
	 * @param schemaCode
	 * @param dto
	 * @return
	 */
	public static Object save(Class<?> instanceClass, String schemaCode, DTObject dto) {
		if (instanceClass == DTObject.class)
			return dto.clone();
		TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceClass, schemaCode);
		return typeInfo.deserialize(dto);
	}

	/// <summary>
	/// 根据架构代码，将dto的数据写入到新实例<paramref name="instanceType"/>中
	/// </summary>
	/// <param name="instance"></param>
	/// <param name="schemaCode"></param>
	/// <param name="dto"></param>
	public static void save(Object instance, String schemaCode, DTObject dto) {
		// if (instance.IsNull()) return;
		var instanceType = instance.getClass();
		if (instanceType.equals(DTObject.class))
			instance = dto.clone();
		TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceType, schemaCode);
		typeInfo.deserialize(instance, dto);
	}

	/**
	 * 据架构代码将对象的信息创建dto
	 * 
	 * @param schemaCode
	 * @param instance
	 * @return
	 */
	public static DTObject load(String schemaCode, Object instance) {
		if (instance == null)
			return DTObject.Empty;
		var instanceType = instance.getClass();
		if (instanceType.equals(DTObject.class))
			return (DTObject) instance;
		TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceType, schemaCode);
		return typeInfo.serialize(instance);
	}

	/**
	 * 根据架构代码将对象的信息加载到dto中
	 * 
	 * @param dto
	 * @param schemaCode
	 * @param instance
	 */
	public static void load(DTObject dto, String schemaCode, Object instance) {
		if (instance == null)
			return;
		var instanceType = instance.getClass();
		if (instanceType.equals(DTObject.class))
			return;
		TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceType, schemaCode);
		typeInfo.serialize(instance, dto);
	}
}