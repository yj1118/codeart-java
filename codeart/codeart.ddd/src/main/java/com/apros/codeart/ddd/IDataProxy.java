package com.apros.codeart.ddd;

public interface IDataProxy extends INotNullObject {

	Object load(DomainProperty property);

	/**
	 * 加载属性更改前的值
	 * 
	 * @param property
	 * @return
	 */
	Object loadOld(DomainProperty property);

	void save(DomainProperty property, Object newValue, Object oldValue);

	/// <summary>
	/// 属性的数据是否已被加载
	/// </summary>
	/// <param name="property"></param>
	/// <returns></returns>
	boolean isLoaded(DomainProperty property);

	DomainObject getOwner();

	void setOwner(DomainObject owner);

	/// <summary>
	/// 拷贝数据代理中的数据
	/// </summary>
	/// <param name="target"></param>
	void copy(IDataProxy target);

	void clear();

	/**
	 * 
	 * 对象是否为快照
	 * 
	 * @return
	 */
	boolean isSnapshot();

	/**
	 * 对象是否为镜像
	 * 
	 * @return
	 */
	boolean isMirror();

	/// <summary>
	/// 数据版本号
	/// </summary>
	int version();

	void version(int value);

	/**
	 * 同步版本号
	 */
	void syncVersion();
}
