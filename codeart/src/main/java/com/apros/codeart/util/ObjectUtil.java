package com.apros.codeart.util;

import com.apros.codeart.runtime.TypeUtil;

public final class ObjectUtil {

	private ObjectUtil() {
	}

	/// <summary>
	/// 判断对象是否为null，这里会使用INullProxy
	/// </summary>
	/// <param name="obj"></param>
	/// <returns></returns>
	public static boolean isNull(Object obj) {
		if (obj == null)
			return true;
		var proxy = TypeUtil.as(obj, INullProxy.class);
		if (proxy != null)
			return proxy.isNull();
		return false;
	}

}
