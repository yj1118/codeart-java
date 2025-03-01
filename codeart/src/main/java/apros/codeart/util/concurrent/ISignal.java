package apros.codeart.util.concurrent;

import java.util.concurrent.TimeUnit;

public interface ISignal<T> {

    T wait(long timeout, TimeUnit unit) throws SignalTimeoutException;

    /**
     * 设置一个信号量
     *
     * @param value
     */
    void set(T value);
}
