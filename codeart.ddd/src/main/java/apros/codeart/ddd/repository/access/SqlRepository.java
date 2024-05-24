package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.AbstractRepository;
import apros.codeart.ddd.repository.DataContext;

public abstract class SqlRepository<TRoot extends IAggregateRoot> extends AbstractRepository<TRoot> {

//	#region 增删改

	@Override
	protected void persistAddRoot(TRoot obj) {
		DataContext.using(() -> {
			DataPortal.insert((DomainObject) obj);
		});
	}

	@Override
	protected void persistUpdateRoot(TRoot obj) {
		DataContext.using(() -> {
			DataPortal.update((DomainObject) obj);
		});
	}

	@Override
	protected void persistDeleteRoot(TRoot obj) {
		DataContext.using(() -> {
			DataPortal.delete((DomainObject) obj);
		});
	}

//	#endregion

	@Override
	public IAggregateRoot findRoot(Object id, QueryLevel level) {
		return DataPortal.querySingle(this.rootType(), id, level);
	}
}
