package apros.codeart.gate.service;

import apros.codeart.App;
import apros.codeart.PreApplicationStart;

@PreApplicationStart()
public class PreStart {
	public static void initialize() {
		App.setup("service");
	}
}