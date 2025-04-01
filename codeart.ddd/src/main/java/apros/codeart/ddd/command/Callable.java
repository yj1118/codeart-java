package apros.codeart.ddd.command;

import apros.codeart.ddd.repository.DataContext;

public abstract class Callable<T> implements ICallable<T> {
    public T execute() {
        return DataContext.using(this::executeImpl);
    }

    protected T executeImpl() {
        return null;
    }
}