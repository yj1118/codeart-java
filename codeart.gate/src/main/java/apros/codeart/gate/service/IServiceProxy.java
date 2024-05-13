package apros.codeart.gate.service;

import apros.codeart.dto.DTObject;

public interface IServiceProxy {
	DTObject invoke(String serviceName, DTObject arg);
}
