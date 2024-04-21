package apros.codeart.mq.rpc;

import apros.codeart.dto.DTObject;
import apros.codeart.mq.TransferData;

public interface IClient {

	TransferData invoke(String method, DTObject arg);

	/**
	 * 清理客户端资源
	 */
	void clear();

}
