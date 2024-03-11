package com.apros.codeart.dto;

import java.util.HashMap;
import java.util.function.Function;

import com.apros.codeart.util.LazyIndexer;

final class TypeIndex {

	private HashMap<String, TypeEntry> _index = new HashMap<String, TypeEntry>();

	private Function<String, String> _getCompleteName;

	public TypeIndex() {

	}

	public void add(TypeEntry entry) {
		String entryTypeName = entry.getTypeName().toLowerCase();
		if (!_index.containsKey(entryTypeName))
			_index.put(entryTypeName, entry);

		if (_index.size() == 1) {
			final String rootTypeName = entry.getName(); // 记录根类型的名称
			final String rootTypeNameDot = String.format("{0}.", rootTypeName);

			_getCompleteName = LazyIndexer.init((typeName) -> {
				if (typeName.startsWith(rootTypeNameDot))
					return typeName; // 已经包含根路径了
				return String.format("{0}.{1}", rootTypeName, typeName);
			});

		}
	}

	public TypeEntry get(String typeName) {
		var e = _index.get(typeName);
		if (e != null)
			return e;
		if (_getCompleteName == null)
			return null;
		// 因为有可能成员采用的简写（不带根的名称）,所以我们需要用完整名称再匹配一次
		var completeName = _getCompleteName.apply(typeName);
		if (completeName.length() == typeName.length())
			return null;
		return _index.get(completeName);
	}

	public boolean contains(String typeName) {
		return _index.containsKey(typeName);
	}

}
