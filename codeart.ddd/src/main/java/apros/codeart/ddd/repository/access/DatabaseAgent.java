package apros.codeart.ddd.repository.access;

import java.util.HashMap;
import java.util.Map;

import apros.codeart.util.SafeAccessImpl;

public abstract class DatabaseAgent implements IDatabaseAgent {

	private Map<Class<? extends IQueryBuilder>, IQueryBuilder> _queryBuilders = new HashMap<>();

	public <T extends IQueryBuilder> void registerQueryBuilder(Class<T> qbClass, IQueryBuilder builder) {
		SafeAccessImpl.checkUp(builder);
		_queryBuilders.put(qbClass, builder);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IQueryBuilder> T getQueryBuilder(Class<T> qbClass) {
		return (T) _queryBuilders.get(qbClass);
	}
}
