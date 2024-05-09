package apros.codeart.service;

import apros.codeart.dto.DTObject;
import apros.codeart.util.StringUtil;

class ServiceRequest {
	private String _name;

	public String name() {
		return _name;
	}

	private DTObject _data;

	public DTObject data() {
		return _data;
	}

	private int _transmittedLength;

	/**
	 * 在含有二进制数据的服务中，已经传输了的二进制数据的长度
	 * 
	 * 注意二进制数据有可能分多次传输，这个值记录多次传输的总长度，而不是本次的长度
	 * 
	 * 不含二进制的数据，该值默认为0
	 * 
	 * @return
	 */
	public int transmittedLength() {
		return _transmittedLength;
	}

	public ServiceRequest(String serviceName, DTObject data, int transmittedLength) {
		_name = serviceName;
		_data = data;
		_transmittedLength = transmittedLength;
	}

//	#region 静态成员

	/**
	 * 
	 * 根据dto定义，得到ServiceRequest
	 * 
	 * @param dto
	 * @return
	 */
	public static ServiceRequest create(DTObject dto) {
		var serviceName = dto.getString("serviceName", StringUtil.empty());
		var data = dto.getObject("data", DTObject.Empty);
		var transmittedLength = data.getInt("transmittedLength", 0);
		return new ServiceRequest(serviceName, data, transmittedLength);
	}

//	#endregion
}
