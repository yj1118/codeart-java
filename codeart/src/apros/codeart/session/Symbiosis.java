package apros.codeart.session;

import static apros.codeart.runtime.Util.any;
import static apros.codeart.runtime.Util.as;

import java.util.HashSet;

import apros.codeart.IReusable;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.PoolItemPhase;

final class Symbiosis implements IReusable {

	private HashSet<Object> _items = new HashSet<Object>();

	private Symbiosis() {
	}

	public void add(Object item) {
		// 只用回收和释放，所以没有实现这两个接口的对象不用加入到集合中
		if (any(item, IReusable.class, AutoCloseable.class)) {
			_items.add(item);
		}
	}

	public int getCount() {
		return _items.size();
	}

	@Override
	public void clear() {
		for (var item : _items) {
			try {
				var reusable = as(item, IReusable.class);
				if (reusable != null) {
					reusable.clear();
					break;
				}

				var closeable = as(item, AutoCloseable.class);
				if (closeable != null) {
					closeable.close();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		_items.clear();
	}

	private static Pool<Symbiosis> _pool = new Pool<Symbiosis>(() -> {
		return new Symbiosis();
	}, (sym, phase) -> {
		if (phase == PoolItemPhase.Returning) {
			sym.clear();
		}
		return true;
	}, PoolConfig.onlyMaxRemainTime(300));

	private final static String _sessionKey = "__Symbiosis.Current";

	/**
	 * 当前应用程序会话中是否存在共生器
	 * 
	 * @return
	 */
	private static boolean ExistsCurrent() {
		return Session.exists() && Session.getItem(_sessionKey) != null;
	}

	/**
	 * 获取或设置当前会话的数据上下文
	 * 
	 * @return
	 */
	public static IPoolItem<Symbiosis> getCurrent() {
		Session.<IPoolItem<Symbiosis>>obtainItem(_sessionKey, () -> {
			return _pool.borrow();
		});
	}

	public static void setCurrent(Symbiosis obj) {
		Session.setItem(_sessionKey, obj);
	}

	/// <summary>
	/// 关闭当前共生器
	/// </summary>
	public static void close() throws Exception {
		if (ExistsCurrent()) {
			var sym = Session.<IPoolItem<Symbiosis>>getItem(_sessionKey);
			_pool.back(sym);
		}
	}

}
