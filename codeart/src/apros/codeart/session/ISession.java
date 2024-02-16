package apros.codeart.session;

import apros.codeart.IReusable;

public interface ISession extends IReusable {

	/**
	 * 指示会话是否无效
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
