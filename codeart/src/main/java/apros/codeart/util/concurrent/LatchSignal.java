package apros.codeart.util.concurrent;

import static apros.codeart.runtime.Util.propagate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class LatchSignal<T> implements ISignal<T> {

    private final CountDownLatch _ready = new CountDownLatch(1);

    private volatile T _value;

    public LatchSignal() {
    }

    public T wait(long timeout, TimeUnit unit) {

        try {
            boolean completed = _ready.await(timeout, unit);
            if (completed) {
                return _value;
            } else {
                throw new SignalTimeoutException();
            }
        } catch (InterruptedException e) {
            throw propagate(e);
        }
    }

    public T forever() {
        try {
            _ready.await(); // 无限等待
            return _value;
        } catch (InterruptedException e) {
            throw propagate(e);
        }
    }

    /**
     * 设置一个信号量
     *
     * @param value
     */
    public void set(T value) {
        _value = value;
        _ready.countDown();
    }
}
