package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.MapList;

class RootIsSlaveIndex {

	private static MapList<String, DataTable> _records = new MapList<String, DataTable>(false,
			(DataTable x, DataTable y) -> {
				return x.name().equals(y.name());
			});

	/**
	 * 当middle的slave是根对象时，追加记录;
	 * 
	 * 如果从表是根，那么需要记录从表和中间表的联系，当删除根对象时，会删除该中间表的数据
	 * 
	 * @param middle
	 */
	public static void tryAdd(DataTable middle) {
		var slave = middle.slave();
		if (!slave.isAggregateRoot())
			return;

		_records.tryPut(slave.name(), middle);
	}

	public static Iterable<DataTable> get(DataTable slave) {
		if (!slave.isAggregateRoot())
			return ListUtil.empty();
		return _records.getValues(slave.name());
	}

}
