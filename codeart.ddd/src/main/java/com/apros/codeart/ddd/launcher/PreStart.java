package com.apros.codeart.ddd.launcher;

import com.apros.codeart.ActionPriority;
import com.apros.codeart.PreApplicationStart;

@PreApplicationStart(ActionPriority.Low)
public class PreStart {
	public static void initialize() {
		DomainHost.initialize();
	}
}
