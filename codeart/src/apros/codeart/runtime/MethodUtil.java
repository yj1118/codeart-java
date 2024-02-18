package apros.codeart.runtime;

import java.lang.reflect.Method;

public final class MethodUtil {
	private MethodUtil() {
	}

	public static Method get(String fullMethodName)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		var lastDot = fullMethodName.lastIndexOf(".");
		var className = fullMethodName.substring(0, lastDot);
		var methodName = fullMethodName.substring(lastDot + 1);
		var cls = Class.forName(className);
		return cls.getMethod(methodName);
	}
}
