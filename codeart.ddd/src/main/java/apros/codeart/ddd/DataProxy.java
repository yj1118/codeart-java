package apros.codeart.ddd;

import java.util.HashMap;
import java.util.Map;

import apros.codeart.runtime.TypeUtil;

/**
 * 我们将领域数据分为两部分存放 一部分是聚合模型内部的成员、另外一部分是引用的聚合根
 * 由于每个聚合根对象都在领域缓冲区，那么当一个聚合根对象由于失效离开了领域缓冲区，而另外一个在缓冲区内聚合根还持有它的引用
 * 这时候就会造成不一致性，因此，引用聚合根的数据我们放在appSession里，这样同一线程可以重复使用外部聚合根,如果需要知道聚合根是否失效可以通过IsSnapshot判断
 * 而不同的线程会在本线程第一次使用外部聚合根时尝试再次加载聚合根，保证一致性
 */
public abstract class DataProxy implements IDataProxy {

	private Map<String, Object> _data;

	/**
	 * 用于记录属性更改情况的数据
	 */
	private Map<String, Object> _oldData;

	public DataProxy() {
		_data = new HashMap<String, Object>();

		if (this.isTrackPropertyChange())
			_oldData = new HashMap<String, Object>();
	}

	public abstract boolean isSnapshot();

	public abstract boolean isMirror();

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract void syncVersion();

	/// <summary>
	/// 因为数据代理可能包含一些线程公共资源，这些资源在对象过期后可以及时清除，腾出内存空间
	/// 数据代理中AppSession就是典型的例子
	/// </summary>
	public void clear() {
		_data.clear();
		if (_oldData != null)
			_oldData.clear();
	}

	public Object load(String propertyName) {
		return _data.get(propertyName);
	}

	public Object loadOld(String propertyName) {
		if (_oldData == null)
			return null;
		return _oldData.get(propertyName);
	}

	public boolean isEmpty() {
		return false;
	}

	public void save(String propertyName, Object newValue, Object oldValue) {
		_data.put(propertyName, newValue);
		if (this.isTrackPropertyChange())
			_oldData.put(propertyName, oldValue);
	}

	public void copy(IDataProxy target) {
		var t = TypeUtil.as(target, DataProxy.class);
		if (t == null)
			return;

		for (var p : t._data.entrySet()) {
			if (!_data.containsKey(p.getKey())) {
				_data.put(p.getKey(), p.getValue());
			}
		}

		if (this.isTrackPropertyChange()) {
			for (var p : t._oldData.entrySet()) {
				if (!_oldData.containsKey(p.getKey())) {
					_oldData.put(p.getKey(), p.getValue());
				}
			}

		}
	}

	/**
	 * 加载数据
	 * 
	 * @param property
	 * @return
	 */
	protected abstract Object loadData(String propertyName);

	public boolean isLoaded(String propertyName) {
		return _data.containsKey(propertyName);
	}

	/**
	 * 创建用于存储的数据代理（不能加载数据，只能存储数据）
	 * 
	 * @param owner
	 * @return
	 */
	public static DataProxy createStorage(DomainObject owner) {
		var proxy = new DataProxyStorage();
		proxy.setOwner(owner);
		return proxy;
	}

	/**
	 * 
	 * 获取该数据代理所属的领域对象实例
	 * 
	 */
	private DomainObject _owner;

	public DomainObject getOwner() {
		return _owner;
	}

	public void setOwner(DomainObject owner) {
		_owner = owner;
	}

	private boolean isTrackPropertyChange() {
		return this._owner == null ? false : _owner.isTrackPropertyChange();
	}

	private static final class DataProxyStorage extends DataProxy {

		public DataProxyStorage() {
		}

		@Override
		protected Object loadData(String propertyName) {
			return null;
		}

		@Override
		public boolean isSnapshot() {
			return false;
		}

		@Override
		public boolean isMirror() {
			return false;
		}

		@Override
		public int getVersion() {
			return 0;
		}

		@Override
		public void setVersion(int version) {
			throw new IllegalStateException("setVersion");
		}

		@Override
		public void syncVersion() {

		}

	}

	private static final class DataProxyEmpty extends DataProxy {

		private DataProxyEmpty() {
		}

		@Override
		public void save(String propertyName, Object newValue, Object oldValue) {

		}

		@Override
		protected Object loadData(String propertyName) {
			return null;
		}

		@Override
		public boolean isSnapshot() {
			return false;
		}

		@Override
		public boolean isMirror() {
			return false;
		}

		@Override
		public int getVersion() {
			return 0;
		}

		@Override
		public void setVersion(int version) {
			throw new IllegalStateException("setVersion");
		}

		@Override
		public void syncVersion() {

		}

		@Override
		public boolean isEmpty() {
			return true;
		}

	}

	public static final DataProxy Empty = new DataProxyEmpty();

}
