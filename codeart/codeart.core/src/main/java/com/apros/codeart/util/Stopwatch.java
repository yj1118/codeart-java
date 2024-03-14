package com.apros.codeart.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class Stopwatch {

	private Stopwatch() {

	}

	public static Duration using(Runnable action) {
		long startTime = System.currentTimeMillis();
		// 执行的代码
		action.run();
		long endTime = System.currentTimeMillis();
		return Duration.ofMillis(endTime - startTime);
	}

	public static Duration using(Runnable action, int times) {
		long startTime = System.currentTimeMillis();
		for (var i = 0; i < times; i++)
			action.run();
		long endTime = System.currentTimeMillis();
		return Duration.ofMillis(endTime - startTime);
	}

	public static List<Duration> using(int times, Runnable... actions) {
		ArrayList<Duration> ds = ListUtil.map(actions, (action) -> {
			return using(action, times);
		});
		return ds;
	}

}
