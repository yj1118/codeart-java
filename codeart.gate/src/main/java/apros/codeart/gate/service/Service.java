package apros.codeart.gate.service;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import apros.codeart.dto.DTObject;
import apros.codeart.util.thread.Task;

public final class Service {

//	#region 同步

	public static DTObject invoke(String serviceName) {
		return invoke(serviceName, DTObject.Empty);
	}

	public static DTObject invoke(String serviceName, Consumer<DTObject> fillArg) {
		var arg = DTObject.editable();
		fillArg.accept(arg);
		return ServiceProxyFactory.get().invoke(serviceName, arg);
	}

	public static DTObject invoke(String serviceName, DTObject arg) {
		return ServiceProxyFactory.get().invoke(serviceName, arg);
	}

	public static Future<DTObject> invokeAsync(String serviceName, DTObject arg) {

		return Task.run(() -> {
			return ServiceProxyFactory.get().invoke(serviceName, arg);
		});
	}
}
