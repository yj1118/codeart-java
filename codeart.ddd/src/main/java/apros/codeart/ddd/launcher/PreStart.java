package apros.codeart.ddd.launcher;

import apros.codeart.ActionPriority;
import apros.codeart.PreApplicationStart;

@PreApplicationStart(ActionPriority.Low)
public class PreStart {
	public static void initialize() {
		DomainHost.initialize();
	}
}
