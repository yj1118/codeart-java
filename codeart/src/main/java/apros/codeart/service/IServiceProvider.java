package apros.codeart.service;

import apros.codeart.dto.DTObject;

public interface IServiceProvider {

	/**
	 * 
	 * 获得服务名称
	 * 
	 * @return
	 */
	String getName();

	DTObject invoke(DTObject arg);
}
