package apros.codeart.ddd.repository.access;

import java.util.ArrayList;

public class TempDataTableIndex {
	private ArrayList<String> _tableKeys;

	public TempDataTableIndex() {
		_tableKeys = new ArrayList<>();
	}

	/**
	 * 尝试将表加入到临时索引中，如果表不存在于索引中，那么可以成功添加到索引，并返回true 否则返回false
	 * 
	 * @param table
	 * @return
	 */
	public boolean tryAdd(DataTable table) {
		if (_tableKeys.contains(table.id()))
			return false;
		_tableKeys.add(table.id());
		return true;
	}

	private void clear() {
		_tableKeys.clear();
	}

}
