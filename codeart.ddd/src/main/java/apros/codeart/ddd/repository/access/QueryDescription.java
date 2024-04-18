package apros.codeart.ddd.repository.access;

import java.util.HashMap;
import java.util.Map;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;

/**
 * {@code param} 查询参数 {@code tables} 涉及到的表 {@code items} 额外的说明项
 * 
 */
public record QueryDescription(MapData param, Map<String, Object> items, DataTable... tables) {

	public QueryDescription(DataTable... tables) {
		this(null, null, tables);
	}

	public QueryDescription(MapData param, DataTable... tables) {
		this(param, null, tables);
	}

	public QueryDescription(Map<String, Object> items) {
		this(null, items);
	}

	public DataTable table() {
		return table(0);
	}

	public DataTable table(int index) {
		if (tables == null)
			return null;
		return tables[index];
	}

	@SuppressWarnings("unchecked")
	public <T> T getItem(String itemName) {
		if (items == null)
			return null;
		return (T) items.get(itemName);
	}

	/**
	 * 为对象表达式创建的查询描述
	 * 
	 * @param param
	 * @param express
	 * @param level
	 * @param table
	 * @return
	 */
	public static QueryDescription createBy(MapData param, String express, QueryLevel level, DataTable table) {
		var items = new HashMap<String, Object>();
		items.put("express", express);
		items.put("level", level);
		return new QueryDescription(param, items, table);
	}

}
