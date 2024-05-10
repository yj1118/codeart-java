package apros.codeart.echo.rpc;

import apros.codeart.dto.DTObject;

public interface IClient {

	DTObject invoke(String method, DTObject arg);

	/**
	 * 清理客户端资源
	 */
	void clear();

}
