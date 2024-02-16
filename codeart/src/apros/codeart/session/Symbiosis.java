package apros.codeart.session;

import static apros.codeart.runtime.Util.any;
import static apros.codeart.runtime.Util.as;

import java.util.HashSet;

import apros.codeart.IReusable;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.PoolItemPhase;

final class Symbiosis implements IReusable {

	private HashSet<Object> _items = new HashSet<Object>();

	/**
	 * 打开共生器的次数
	 */
	private int OpenTimes;

	private Symbiosis() {
		this.OpenTimes = 0;
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

	public static Pool<Symbiosis> Pool = new Pool<Symbiosis>(() -> {
		return new Symbiosis();
	}, (sym, phase) -> {
		if (phase == PoolItemPhase.Returning) {
			sym.clear();
		}
		return true;
	}, PoolConfig.onlyMaxRemainTime(300));

	private final String _sessionKey = "__Symbiosis.Current";

	/// <summary>
	/// 当前应用程序会话中是否存在共生器
	/// </summary>
	/// <returns></returns>
	private static boolean ExistsCurrent() {
		return Session.exists() && Session.getItem < Symbiosis > (_sessionKey) != null;
	}

	/// <summary>
	/// 打开共生器
	/// </summary>
	public static Symbiosis Open() {
		Symbiosis sym;
		if (ExistsCurrent()) {
			sym = Current;
		} else {
			sym = _pool.Borrow();
			Current = sym;
		}
		sym.OpenTimes++;
		return sym;
	}

	/// <summary>
	/// 关闭当前共生器
	/// </summary>
	public static void Close() {
		Symbiosis sym = Current;
		sym.OpenTimes--;
		if (sym.OpenTimes == 0) {
			_pool.Return(sym);
			Current = null;
		}
	}

}
