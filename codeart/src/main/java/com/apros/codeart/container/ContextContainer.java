package com.apros.codeart.container;

/**
 * 上下文容器
 * 
 * 该容器返回的对象在一个会话上下文中单例
 */
public final class ContextContainer {
	private ContextContainer() {
	}

	public static <T> T getInstance() {
		return null;
	}
}