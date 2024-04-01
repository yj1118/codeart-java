package com.apros.codeart;

import java.util.ArrayList;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.ResourceUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public final class AppConfig {

	private AppConfig() {

	}

	private static Iterable<String> _archives;

	public static String[] mergeArchives(String... appendArchives) {

		if (appendArchives != null && appendArchives.length > 0) {
			return mergeArchives(archives(), appendArchives);
		}

		return Iterables.toArray(archives(), String.class);
	}

	public static Iterable<String> archives() {
		if (_archives == null) {
			_archives = ImmutableList.copyOf(_config.getStrings("archives"));
		}
		return _archives;
	}

	private static String[] mergeArchives(Iterable<String> source, String[] append) {
		if (append.length == 0)
			return Iterables.toArray(source, String.class);
		ArrayList<String> result = new ArrayList<String>();
		ListUtil.addRange(result, source, true);
		ListUtil.addRange(result, append, true); // 过滤重复项
		return Iterables.toArray(result, String.class);
	}

	private static final DTObject _config;

	static {
		var configCode = ResourceUtil.load("config/app.json");
		_config = DTObject.readonly(configCode);
	}

}
