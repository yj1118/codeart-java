package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.AbstractRepository;
import apros.codeart.ddd.repository.DataContext;

public abstract class SqlRepository<TRoot extends IAggregateRoot> extends AbstractRepository<TRoot> {

    //region 增删改

    @Override
    protected void persistAddRoot(TRoot obj) {
        DataContext.using(() -> {
            onPreAdd(obj);
            DataPortal.insert((DomainObject) obj);
            onAdded(obj);
        });
    }

    public void onPreAdd(TRoot obj) {
    }

    public void onAdded(TRoot obj) {
    }

    @Override
    protected void persistUpdateRoot(TRoot obj) {
        DataContext.using(() -> {
            onPreUpdate(obj);
            DataPortal.update((DomainObject) obj);
            onUpdated(obj);
        });
    }

    public void onPreUpdate(TRoot obj) {
    }

    public void onUpdated(TRoot obj) {

    }

    @Override
    protected void persistDeleteRoot(TRoot obj) {
        DataContext.using(() -> {
            onPreDelete(obj);
            DataPortal.delete((DomainObject) obj);
            onDeleted(obj);
        });
    }

    public void onDeleted(TRoot obj) {

    }


    public void onPreDelete(TRoot obj) {

    }


    //endregion

    @Override
    public IAggregateRoot findRoot(Object id, QueryLevel level) {
        return DataPortal.querySingle(this.rootType(), id, level);
    }
}
