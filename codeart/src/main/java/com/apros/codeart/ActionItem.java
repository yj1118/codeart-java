package com.apros.codeart;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.propagate;

import com.apros.codeart.runtime.MethodUtil;

public record ActionItem(Class<?> type, String methodName, ActionPriority priority) {

	public byte priorityValue() {
		return priority.getValue();
	}

	public void run() {

		var method = MethodUtil.resolveByNameOnce(type, methodName);

		if (method == null) {
			throw new IllegalStateException(strings("codeart", "NotFoundMethod", type.getName(), methodName));
		}

		if (!MethodUtil.isStatic(method) || method.getParameterCount() > 0 || !MethodUtil.isPublic(method))
			throw new IllegalStateException(strings("codeart", "LifeMethodError", type.getName(), methodName));

		try {
			method.invoke(null);
		} catch (Exception e) {
			throw propagate(e);
		}
	}
}