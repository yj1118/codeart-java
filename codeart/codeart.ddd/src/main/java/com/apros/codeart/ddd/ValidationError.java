package com.apros.codeart.ddd;

import com.apros.codeart.util.StringUtil;

/**
 * 
 */
public class ValidationError {

	private String _code;

	/**
	 * 获取错误编码
	 * 
	 * @return
	 */
	public String getCode() {
		return _code;
	}

	private String _message;

	/**
	 * 错误的消息
	 * 
	 * @return
	 */
	public String getMessage() {
		return _message;
	}

	ValidationError(String code, String message) {
		_code = code;
		_message = message;
	}

	public boolean isEmpty() {
		return StringUtil.isNullOrEmpty(this.getCode()) && StringUtil.isNullOrEmpty(this.getCode());
	}
}
