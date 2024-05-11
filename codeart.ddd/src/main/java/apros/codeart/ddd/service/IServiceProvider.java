package apros.codeart.ddd.service;

import apros.codeart.dto.DTObject;

public interface IServiceProvider {

	DTObject invoke(DTObject arg);
}