package com.apros.codeart.dto.serialization;

abstract class TypeSerializationInfo {
	private DTOClassAnnotation _classAnn;

	public DTOClassAnnotation getClassAnn() {
		return _classAnn;
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

//	#region 序列化方法
//
//	public SerializeMethod SerializeMethod
//	{
//        get;
//        private set;
//    }
//
//	private SerializeMethod CreateSerializeMethod()
//    {
//        return DTOSerializeMethodGenerator.GenerateMethod(this);
//    }
//
//	#endregion

}
