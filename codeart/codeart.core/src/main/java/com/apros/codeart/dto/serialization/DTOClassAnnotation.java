package com.apros.codeart.dto.serialization;

class DTOClassAnnotation {

	private DTOSerializableMode _mode;

	public DTOSerializableMode mode() {
		return _mode;
	}

	private DTOClassAnnotation(DTOSerializableMode mode) {
		_mode = mode;
	}

	private DTOClassAnnotation() {
		this(DTOSerializableMode.General);
	}

	/**
	 * 获取类型定义的 DTOClassAnnotation 信息
	 * 
	 * @param type
	 * @return
	 */
	public static DTOClassAnnotation get(Class<?> cls) {

		var ann = cls.getAnnotation(DTOClass.class);

		return ann != null ? new DTOClassAnnotation(ann.mode()) : Default;
	}

	public static final DTOClassAnnotation Default = new DTOClassAnnotation();
}
