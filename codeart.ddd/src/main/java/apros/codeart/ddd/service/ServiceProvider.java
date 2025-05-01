package apros.codeart.ddd.service;

import apros.codeart.ddd.repository.Page;
import apros.codeart.dto.DTObject;

public abstract class ServiceProvider implements IServiceProvider {

    public abstract DTObject invoke(DTObject arg);

}
