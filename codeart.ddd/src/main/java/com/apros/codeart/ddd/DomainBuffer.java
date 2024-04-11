package com.apros.codeart.ddd;

import com.apros.codeart.ddd.repository.DataContext;

public final class DomainBuffer {
	private DomainBuffer() {
	}

	public static void add(IAggregateRoot root) {
		add(root, false);
	}

	public static void add(IAggregateRoot root, boolean isMirror) {
		if (DataContext.existCurrent())
			DataContext.getCurrent().addBuffer(root, isMirror); // 加入数据上下文缓冲区
	}
}
