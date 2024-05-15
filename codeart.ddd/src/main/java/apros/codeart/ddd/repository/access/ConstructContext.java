package apros.codeart.ddd.repository.access;

import java.util.ArrayList;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.DomainObject;
import apros.codeart.util.ListUtil;

final class ConstructContext {

	private ConstructContext() {
	}

	public static Object get(Class<?> objectType, Object id) {
		return getCurrent().get(objectType, id, id);
	}

	/**
	 * 
	 * 从构造上下文中获取对象
	 * 
	 * @param objectType
	 * @param rootId
	 * @param id
	 * @return
	 */
	public static Object get(Class<?> objectType, Object rootId, Object id) {
		return getCurrent().get(objectType, rootId, id);
	}

	public static void add(Object rootId, Object id, DomainObject obj) {
		getCurrent().add(rootId, id, obj);
	}

	public static void add(Object id, DomainObject obj) {
		getCurrent().add(id, id, obj);
	}

	public static void remove(DomainObject obj) {
		getCurrent().remove(obj);
	}

	private static class ConstructContextImpl {
		private ArrayList<Item> _items;

		public ConstructContextImpl() {
			_items = new ArrayList<Item>();
		}

		public void add(Object rootId, Object id, DomainObject obj) {
			var item = new Item(obj.getClass(), rootId, id, obj);
			_items.add(item);
		}

		public void remove(Object obj) {
			ListUtil.removeFirst(_items, (t) -> {
				return t.target() == obj; // 直接根据内存地址比较即可
			});
		}

		/// <summary>
		/// 从构造上下文中获取对象
		/// </summary>
		/// <param name="id"></param>
		/// <returns></returns>
		public Object get(Class<?> objectType, Object rootId, Object id) {

			var item = ListUtil.find(_items, (t) -> {
				return t.objectType().equals(objectType) && t.rootId().equals(rootId) && t.id().equals(id);
			});
			return item == null ? null : item.target();
		}

		public void clear() {
			_items.clear();
		}
	}

	private static record Item(Class<?> objectType, Object rootId, Object id, Object target) {
	}

//	#region 基于当前应用程序回话的数据上下文

	private final static String _sessionKey = "__ConstructContext.Current";

	/**
	 * 获取或设置当前会话的数据上下文
	 * 
	 * @return
	 */
	private static ConstructContextImpl getCurrent() {
		return AppSession.obtainItem(_sessionKey, () -> {
			return new ConstructContextImpl();
		});
	}

}
