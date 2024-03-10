package com.apros.codeart.pooling;

public class PoolingException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6500211809452215882L;

	// 构造函数
	public PoolingException() {
		super();
	}

	// 带有消息的构造函数
	public PoolingException(Exception innerException) {
		super("", innerException);
	}

	public PoolingException(String message, Exception innerException) {
		super(message, innerException);
	}

	public PoolingException(String message) {
		super(message);
	}
}