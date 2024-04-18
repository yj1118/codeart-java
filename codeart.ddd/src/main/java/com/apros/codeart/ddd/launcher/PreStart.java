package com.apros.codeart.ddd.launcher;

import com.apros.codeart.ActionPriority;
import com.apros.codeart.PreApplicationStart;
import com.apros.codeart.ddd.dynamic.IDynamicRepository;
import com.apros.codeart.ddd.repository.Repository;
import com.apros.codeart.ddd.repository.access.SqlDynamicRepository;

@PreApplicationStart(ActionPriority.Low)
public class PreStart {
	public static void initialize() {
		DomainHost.initialize();

		// 注入动态仓储的支持
		Repository.register(IDynamicRepository.class, SqlDynamicRepository.Instance);
	}
}
