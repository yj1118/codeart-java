package apros.codeart.dto.serialization;

class DTOClassAnn {

	private DTOSerializableMode _mode;

	public DTOSerializableMode mode() {
		return _mode;
	}

	private DTOClassAnn(DTOSerializableMode mode) {
		_mode = mode;
	}

	private DTOClassAnn() {
		this(DTOSerializableMode.General);
	}

	/**
	 * 获取类型定义的 DTOClassAnnotation 信息
	 * 
	 * @param type
	 * @return
	 */
	public static DTOClassAnn get(Class<?> cls) {

		var ann = cls.getAnnotation(DTOClass.class);

		return ann != null ? new DTOClassAnn(ann.mode()) : Default;
	}

	public static final DTOClassAnn Default = new DTOClassAnn();
}
