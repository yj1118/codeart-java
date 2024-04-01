package com.apros.codeart;

import java.lang.annotation.Annotation;

import com.apros.codeart.runtime.Activator;
import com.apros.codeart.util.ArgumentAssert;
import com.apros.codeart.util.ListUtil;

public final class App {
	private App() {
	}

	private static boolean _process_pre_start_completed;
	private static Object _syncObject = new Object();

	private static String[] _archives;

	/**
	 * 应用程序初始化，请根据不同的上下文环境，在程序入口处调用此方法
	 * 
	 * @param archives 需要参与初始化的档案名称，档案是包的顶级名称，比如
	 *                 subsystem.account和subsystem.user的档案名为subsystem
	 */
	public static void initialize(String... archives) {
		if (_process_pre_start_completed)
			return;

		ArgumentAssert.isNotNullOrEmpty(archives, "archives");

		synchronized (_syncObject) {
			if (_process_pre_start_completed)
				return;
			_process_pre_start_completed = true;
			_archives = AppConfig.mergeArchives(archives);
			process_pre_start();
		}
	}

	private static void process_pre_start() {
		runActions(PreApplicationStart.class);
	}

	private static boolean _process_post_start_completed = false;

	/// <summary>
	/// 应用程序初始化完后，请根据不同的上下文环境，在程序入口处调用此方法
	/// </summary>
	public static void initialized() {
		if (_process_post_start_completed)
			return;
		synchronized (_syncObject) {
			if (_process_post_start_completed)
				return;
			_process_post_start_completed = true;
			process_post_start();
		}
	}

	private static void process_post_start() {
		runActions(PostApplicationStart.class);
	}

	private static boolean _process_pre_end_completed = false;

	public static void dispose() {
		if (_process_pre_end_completed)
			return;
		synchronized (_syncObject) {
			if (_process_pre_end_completed)
				return;
			_process_pre_end_completed = true;
			process_pre_end();
		}
	}

	private static void process_pre_end() {
		runActions(PreApplicationEnd.class);
	}

	private static boolean _process_post_end_completed = false;

	public static void disposed() {
		if (_process_post_end_completed)
			return;
		synchronized (_syncObject) {
			if (_process_post_end_completed)
				return;
			_process_post_end_completed = true;
			process_post_end();
		}
	}

	private static void process_post_end() {
		runActions(PostApplicationEnd.class);
	}

	private static void runActions(Class<? extends Annotation> annType) {
		var items = ListUtil.map(Activator.getAnnotatedTypesOf(annType, _archives), (type) -> {
			var ann = type.getAnnotation(PreApplicationStart.class);
			return new ActionItem(type, ann.method(), ann.value());
		});

		items.sort((s1, s2) -> {

			if (s1.priorityValue() > s2.priorityValue())
				return -1; // s1 在 s2之前
			if (s1.priorityValue() < s2.priorityValue())
				return 1;
			return 0;
		});

		for (var item : items) {
			item.run();
		}
	}

}
