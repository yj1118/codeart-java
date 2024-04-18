package apros.codeart.ddd.launcher;

import apros.codeart.ActionPriority;
import apros.codeart.PreApplicationStart;
import apros.codeart.ddd.dynamic.IDynamicRepository;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.repository.access.SqlDynamicRepository;

@PreApplicationStart(ActionPriority.Low)
public class PreStart {
	public static void initialize() {
		DomainHost.initialize();

		// 注入动态仓储的支持
		Repository.register(IDynamicRepository.class, SqlDynamicRepository.Instance);
	}
}
