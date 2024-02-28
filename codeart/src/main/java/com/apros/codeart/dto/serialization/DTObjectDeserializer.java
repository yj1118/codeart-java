package com.apros.codeart.dto.serialization;

import com.apros.codeart.dto.DTObject;

class DTObjectDeserializer implements IDTObjectDeserializer {
	private DTObjectDeserializer() {
	}

	public Object deserialize(Class<?> objectType, DTObject dto) {
//		if (objectType.equals(DTObject.class))
//			return dto;
//		TypeMakupInfo typeInfo = TypeMakupInfo.getTypeInfo(objectType);
//		return typeInfo.deserialize(dto);
		return null;
	}

	/**
	 * 将dto的内容反序列化到 instance
	 * 
	 * @param instance
	 * @param dto
	 */
	public void deserialize(Object instance, DTObject dto) {
//		TypeMakupInfo typeInfo = TypeMakupInfo.getTypeInfo(instance.getType());
//		typeInfo.deserialize(instance, dto);
	}

	public static final DTObjectDeserializer Instance = new DTObjectDeserializer();

}
