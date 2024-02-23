package com.apros.codeart.pooling;

interface IPoolContainer<R> {

	/**
	 * 获取项
	 * 
	 * @return
	 */
	R take();

	void put(R item);

	int getCount();
}
