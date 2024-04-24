package apros.codeart.dto.serialization.internal;

class DTOClassImpl {

	private DTOClassImpl() {
	}

	/**
	 * 获取类型定义的 DTOClassAnnotation 信息
	 * 
	 * @param type
	 * @return
	 */
	public static DTOClassImpl get(Class<?> cls) {

		return Default;

//		var ann = cls.getAnnotation(DTOClass.class);
//
//		return ann != null ? new DTOClassAnn(ann.mode()) : Default;
	}

	public static final DTOClassImpl Default = new DTOClassImpl();
}
