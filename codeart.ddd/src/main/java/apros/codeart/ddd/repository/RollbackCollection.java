package apros.codeart.ddd.repository;

import java.util.ArrayList;

class RollbackCollection {

	private ArrayList<RepositoryRollbackEventArgs> _items = null;

	public RollbackCollection() {
		_items = new ArrayList<RepositoryRollbackEventArgs>();
	}

	public void add(RepositoryRollbackEventArgs e) {
		_items.add(e);
	}

	/**
	 * 执行回滚
	 * 
	 * @param sender
	 */
	public void execute(IDataContext sender) {
		if (_items.isEmpty())
			return;
		for (var e : _items) {
			e.target().onRollback(sender, e);
			e.repository().onRollback(sender, e);
		}
		this.clear();// 执行完毕后清理
	}

	/**
	 * 清理回滚列表
	 */
	public void clear() {
		_items.clear();
	}
}
