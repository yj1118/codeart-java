package apros.codeart.util.thread;

import static apros.codeart.i18n.Language.strings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Timer {

	private long _period;

	private TimeUnit _unit;

	private ScheduledExecutorService _scheduler;

	public Timer(long period, TimeUnit unit) {
		_period = period;
		_unit = unit;
	}

	public void immediate(Runnable action) {
		start(0, action);
	}

	public void delay(long initialDelay, Runnable action) {
		start(initialDelay, action);
	}

	private void start(long initialDelay, Runnable action) {
		if (_scheduler != null)
			throw new IllegalArgumentException(strings("codeart", "TimerWorking"));
		// 创建一个单线程的ScheduledExecutorService
		_scheduler = Executors.newScheduledThreadPool(1);
		_scheduler.scheduleAtFixedRate(action, initialDelay, _period, _unit);
	}

	public void stop() {
		if (_scheduler != null) {
			_scheduler.shutdown();
			_scheduler = null;
		}
	}

}
