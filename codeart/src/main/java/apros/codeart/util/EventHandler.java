package apros.codeart.util;

import java.util.ArrayList;
import java.util.function.Supplier;

public class EventHandler<T> {

    private ArrayList<IEventObserver<T>> _observers;

    public EventHandler() {

    }

    public void add(IEventObserver<T> observer) {
        if (_observers == null)
            _observers = new ArrayList<IEventObserver<T>>();

        if (_observers.contains(observer))
            return;

        _observers.add(observer);
    }

    public void remove(IEventObserver<T> observer) {
        if (this.isEmpty())
            return;

        _observers.remove(observer);
    }

    public void raise(Object sender, Supplier<T> getArgs) {
        if (this.isEmpty())
            return;

        var args = getArgs.get();
        for (var observer : _observers) {
            observer.handle(sender, args);
        }
    }

    /**
     * 移除所有挂载的事件
     */
    public void clear() {
        _observers.clear();
    }

    public boolean isEmpty() {
        return _observers == null || _observers.isEmpty();
    }

}
