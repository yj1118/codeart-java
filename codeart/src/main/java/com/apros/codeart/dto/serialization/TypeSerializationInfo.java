package com.apros.codeart.dto.serialization;

abstract class TypeSerializationInfo {
	private DTOClassAnnotation _classAnnotation;

	public DTOClassAnnotation getClassAnnotation() {
		return _classAnnotation;
	}

	private Class<?> _targetClass;

	/**
	 * 被序列化的类型
	 * 
	 * @return
	 */
	public Class<?> getTargetClass() {
		return _targetClass;
	}

}
