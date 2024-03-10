package com.apros.codeart.dto.serialization;

import java.lang.reflect.Field;

class DTOMemberAnnotation {

	private String _name;

	public String getName() {
		return _name;
	}

	private DTOMemberType _type;

	public DTOMemberType getType() {
		return _type;
	}

	private boolean _isBlob;

	/**
	 * 是否为文件流类型
	 * 
	 * @return
	 */
	public boolean isBlob() {
		return _isBlob;
	}

	public DTOMemberAnnotation(String name, DTOMemberType type, boolean isBlob) {
		_name = name;
		_type = type;
		_isBlob = isBlob;
	}

	public DTOMemberAnnotation(String name, DTOMemberType type) {
		this(name, type, false);
	}

	public static DTOMemberAnnotation get(Field field) {

		var ann = field.getAnnotation(DTOMember.class);

		return ann != null ? new DTOMemberAnnotation(ann.name(), ann.type(), ann.blob()) : null;
	}
}
