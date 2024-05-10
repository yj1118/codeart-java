package apros.codeart.service;

import apros.codeart.dto.DTObject;

public abstract class ServiceProvider implements IServiceProvider {

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public abstract DTObject invoke(DTObject arg);

}
