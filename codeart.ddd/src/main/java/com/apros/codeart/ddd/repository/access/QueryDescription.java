package com.apros.codeart.ddd.repository.access;

import java.util.Map;

import com.apros.codeart.ddd.MapData;

/**
 * {@code param} 查询参数 {@code tables} 涉及到的表 {@code items} 额外的说明项
 */
public record QueryDescription(MapData param, Map<String, Object> items, DataTable... tables) {

	public QueryDescription(DataTable... tables) {
		this(null, null, tables);
	}

	public QueryDescription(MapData param) {
		this(param, null);
	}

	public QueryDescription(Map<String, Object> items) {
		this(null, items);
	}
}
