package com.apros.codeart.dto.serialization;

import com.apros.codeart.dto.DTObject;

public class DTObjectSerializer implements IDTObjectSerializer {
	private DTObjectSerializer() {
	}

	public DTObject serialize(Object instance) {
////		 if (instance == null) return DTObject.Create("{null}"); 老代码，暂时保留
//		if (instance == null)
//			return DTObject.Empty;
//		var instanceType = instance.getClass();
//		if (instanceType.equals(DTObject.class))
//			return (DTObject) instance;
//		TypeMakupInfo typeInfo = TypeMakupInfo.GetTypeInfo(instanceType);
//		return typeInfo.Serialize(instance);
		return null;
	}

	public static final DTObjectSerializer Instance = new DTObjectSerializer();
}
