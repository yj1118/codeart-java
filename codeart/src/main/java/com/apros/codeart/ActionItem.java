package com.apros.codeart;

import static com.apros.codeart.i18n.Language.strings;

import com.apros.codeart.runtime.MethodUtil;

public record ActionItem(Class<?> type,String methodName,ActionPriority priority) {
	
	public byte priorityValue() {
		return priority.getValue();
	}
	
	public void run() {

		var method = MethodUtil.resolveByNameOnce(type,methodName);
		
		if(method == null) {
			throw new IllegalStateException(strings("NotFoundMethod",type.getName(),methodName));
		}
		
		if(!MethodUtil.isStatic(method) || method.getParameterCount() > 0)
			throw new IllegalStateException(strings("LifeMethodError",type.getName(),methodName));
		
    }
}