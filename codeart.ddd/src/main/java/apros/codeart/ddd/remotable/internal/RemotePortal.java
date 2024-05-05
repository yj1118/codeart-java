package apros.codeart.ddd.remotable.internal;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.cqrs.slave.RemoteService;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.dynamic.IDynamicRepository;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.TypeUtil;

public final class RemotePortal {

	private RemotePortal() {
	}

//	region 获取和同步对象

	/**
	 * 
	 * 获取远程对象并根据配置保存到本地
	 * 
	 * @param <T>
	 * @param objectType 远程对象的类型
	 * @param id         远程对象的编号
	 * @param level
	 * @return
	 */
	public static <T extends DynamicRoot> T getObject(Class<T> objectType, Object id, QueryLevel level) {
		var repository = Repository.create(IDynamicRepository.class);
		var root = repository.find(objectType, id, level);
		if (!root.isEmpty())
			return root;

		// 从远程获取聚合根对象
		var remoteRoot = getRootByRemote(objectType, id);

		// 保存到本地
		root = DataContext.newScope(() -> {
			var localRoot = repository.find(objectType, id, QueryLevel.HoldSingle);
			if (localRoot.isEmpty()) {
				addRoot(repository, objectType, remoteRoot);
				localRoot = remoteRoot;
			}
			return remoteRoot;
		});
		return root;
	}

	/**
	 * 
	 * 从远程加载数据
	 * 
	 * @param objectType
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T extends DynamicRoot> T getRootByRemote(Class<T> objectType, Object id) {
		var data = RemoteService.getObject(objectType, id);

		var obj = (T) Activator.createInstance(objectType);
		obj.load(data);

		return obj;
	}

	/// <summary>
	/// 新增对象到本地仓储
	/// </summary>
	/// <param name="repository"></param>
	/// <param name="define"></param>
	/// <param name="root"></param>
	private static <T extends DynamicRoot> void addRoot(IDynamicRepository repository, Class<T> objectType, T root) {
		repository.addRoot(root);
		addMemberRoots(repository, root);
	}

	private static void addMemberRoots(IDynamicRepository repository, DynamicRoot root) {
		var memberRoots = root.getRefRoots();
		for (var member : memberRoots) {
			var objectType = TypeUtil.asT(member.getClass(), DynamicRoot.class);

			var id = member.getIdentity();
			// 为了防止死锁，我们开启的是不带锁的模式判定是否有本地数据
			// 虽然这有可能造成重复输入的插入而导致报错，但是几率非常低，而且不会引起灾难性bug
			var local = repository.find(objectType, id, QueryLevel.None);
			if (local.isEmpty()) {
				repository.addRoot(member);
			}
		}
	}

	/**
	 * 修改远程对象在本地的映射
	 * 
	 * @param objectType
	 * @param id
	 */
	static void updateObject(Class<? extends DynamicRoot> objectType, Object id) {
		// 这里需要注意，我们不能简单的删除本地对象再等待下次访问时加载
		// 因为对象缓存的原因，对象的属性可能引用了已删除的本地对象
		// 这些被引用的以删除的本地对象只有在对象缓存过期后才更新，导致数据更新不及时
		// 因此需要手工更改对象的内容
		var repository = Repository.create(IDynamicRepository.class);

		DataContext.newScope(() -> {
			var local = repository.find(objectType, id, QueryLevel.None);
			if (local.isEmpty())
				return; // 本地没有数据，不需要更新

			local = repository.find(objectType, id, QueryLevel.HoldSingle);
			if (local.isEmpty())
				return; // 本地没有数据，不需要更新

			var root = getRootByRemote(objectType, id);
			if (root.isEmpty()) {
				deleteObject(objectType, id);
				return;
			}
			local.sync(root); // 同步数据
			updateRoot(repository, local);
		});
	}

	private static void updateRoot(IDynamicRepository repository, DynamicRoot root) {
		repository.updateRoot(root); // 保存修改后的数据
		addMemberRoots(repository, root); // 有可能修改后的数据包含新的根成员需要增加
	}

	/// <summary>
	/// 删除远程对象在本地的映射
	/// </summary>
	/// <param name="define"></param>
	/// <param name="id"></param>
	static void deleteObject(Class<? extends DynamicRoot> objectType, Object id) {
		var repository = Repository.create(IDynamicRepository.class);
		var root = repository.find(objectType, id, QueryLevel.Single);
		if (!root.isEmpty()) {
			// 同步删除
			repository.deleteRoot(root);

		}
	}

//	#endregion
//
//	#
//
//	region 广播消息

	/**
	 * 
	 * 通知对象已修改
	 * 
	 * @param type
	 * @param id
	 */
	public static void notifyUpdated(Class<?> objectType, Object id) {
		RemoteService.notifyUpdated(objectType, id);
	}

	/**
	 * 通知对象已删除
	 * 
	 * @param objectType
	 * @param id
	 */
	public static void notifyDeleted(Class<?> objectType, Object id) {
		RemoteService.notifyDeleted(objectType, id);
	}

//	#endregion

}
