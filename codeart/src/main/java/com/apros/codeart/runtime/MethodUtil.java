package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;

import com.apros.codeart.util.StringUtil;

public final class MethodUtil {
	private MethodUtil() {
	}

	public static Method get(String fullMethodName) {
		try {
			var lastDot = fullMethodName.lastIndexOf(".");
			var className = StringUtil.substr(fullMethodName, 0, lastDot);
			var methodName = StringUtil.substr(fullMethodName, lastDot + 1);
			var cls = Class.forName(className);
			return cls.getMethod(methodName);
		} catch (Exception ex) {
			throw propagate(ex);
		}
	}

}
