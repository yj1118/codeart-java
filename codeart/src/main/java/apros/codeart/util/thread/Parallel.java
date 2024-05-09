package apros.codeart.util.thread;

import static apros.codeart.runtime.Util.propagate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.google.common.collect.Iterables;

public final class Parallel {
	private Parallel() {
	}

	public static <T> void forEach(Iterable<T> sources, Consumer<T> handle) {
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		List<Future<?>> futures = new ArrayList<>(Iterables.size(sources));

		for (var source : sources) {

			Future<?> future = executor.submit(() -> {
				handle.accept(source);
			});
			futures.add(future);
		}

		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				throw propagate(e);
			}
		}

		executor.shutdown();
	}

}
