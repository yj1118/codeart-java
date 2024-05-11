package apros.codeart.util.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class Task {
	private Task() {
	}

	private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	public static void run(Runnable action) {
		executor.submit(() -> {
			action.run();
		});
	}

	public static <T> Future<T> run(Supplier<T> action) {
		// 提交一个异步任务
		return executor.submit(() -> {
			return action.get();
		});
	}

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			executor.shutdown(); // 禁止提交新任务，继续执行已提交的任务
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				executor.shutdownNow(); // 尝试立即停止所有正在执行的任务
				Thread.currentThread().interrupt();
			}
		}));
	}

}
