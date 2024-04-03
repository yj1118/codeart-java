package com.apros.codeart.ddd;

public interface IEntityObject extends IDomainObject {

	/**
	 * 获取对象唯一标示 在领域驱动思想中
	 * 
	 * 引用对象的标示可以是本地唯一也可以是全局唯一
	 * 
	 * 在内聚根对象中一定要全局唯一
	 * 
	 * @return
	 */
	Object getIdentity();
}
