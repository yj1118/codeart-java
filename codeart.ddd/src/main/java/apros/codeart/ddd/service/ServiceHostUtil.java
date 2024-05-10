package apros.codeart.ddd.service;

import apros.codeart.dto.DTObject;

final class ServiceHostUtil {
	private ServiceHostUtil() {
	}

	public static final DTObject Success = DTObject.readonly("{\"status\":\"success\"}");

	public static DTObject createFailed(Exception ex) {
		DTObject failed = DTObject.editable();
		failed.setValue("status", "failed");
		failed.setValue("message", ex.getMessage());
		return failed;
	}
}
