package com.apros.codeart.context;

import com.apros.codeart.pooling.IReusable;

public interface IAppSession extends IReusable {

	/**
	 * 指示会话是否有效
	 * 
	 * @return
	 */
	boolean valid();

	/**
	 * 初始化回话
	 * 
	 * @throws Exception
	 */
	void initialize() throws Exception;

	Object getItem(String name);

	void setItem(String name, Object value);

	boolean containsItem(String name);
}
