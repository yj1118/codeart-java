package apros.codeart.echo.rpc;

import apros.codeart.dto.DTObject;

public interface IRPCHandler {
	DTObject process(String method, DTObject args);
}