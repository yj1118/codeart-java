package apros.codeart.ddd.service;

import apros.codeart.ddd.repository.Page;
import apros.codeart.dto.DTObject;

public interface IServiceProvider {

    DTObject invoke(DTObject arg);
}