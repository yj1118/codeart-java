package apros.codeart.ddd.command;

import apros.codeart.ddd.repository.DataContext;

public abstract class Runnable implements IRunnable {
    public void execute() {
        DataContext.using(this::executeImpl);
    }

    protected void executeImpl() {
    }
}
