package com.apros.codeart.runtime;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;

public final class DynamicUtil {
	private DynamicUtil() {
	}

	/**
	 * 获得类的二进制名称（内部名称），例如：java/lang/String
	 * 
	 * @param cls
	 * @return
	 */
	public static String getInternalName(Class<?> cls) {
		return Type.getInternalName(cls);
	}

	/**
	 * 获得类型描述符，例如：I，Ljava/lang/String; 等
	 * 
	 * @param cls
	 * @return
	 */
	public static String getDescriptor(Class<?> cls) {
		return Type.getType(cls).getDescriptor();
	}

	public static String getMethodDescriptor(Method method) {
		return getMethodDescriptor(method.getReturnType(), method.getParameterTypes());
	}

	//

	/**
	 * 获取方法的描述
	 * 
	 * @param returnClass
	 * @param argumentClasses
	 * @return
	 */
	public static String getMethodDescriptor(final Class<?> returnClass, final Class<?>... argumentClasses) {

		Type[] types = new Type[argumentClasses.length];

		for (var i = 0; i < argumentClasses.length; i++) {
			types[i] = Type.getType(argumentClasses[i]);
		}

		// 获取方法的描述符
		return Type.getMethodDescriptor(Type.getType(returnClass), types);

	}

}
