package apros.codeart.ddd.launcher;

import apros.codeart.PreApplicationStart;

@PreApplicationStart()
public class PreStart {
	public static void initialize() {
		DomainHost.initialize();
	}
}
