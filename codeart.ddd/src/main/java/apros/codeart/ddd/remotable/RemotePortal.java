package apros.codeart.ddd.remotable;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.dynamic.IDynamicRepository;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;

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
	static <T extends DynamicRoot> T getObject(Class<T> objectType, Object id, QueryLevel level) {
		var repository = Repository.create(IDynamicRepository.class);
		var root = repository.find(objectType, id, level);
		if (!root.isEmpty())
			return root;

		// 从远程获取聚合根对象
		var remoteRoot = getRootByRemote(objectType, id);

		// 保存到本地
		DataContext.newScope(() -> {
			root = repository.find(objectType, id, QueryLevel.HoldSingle);
			if (root.isEmpty()) {
				root = remoteRoot;
				addRoot(repository, objectType, root);
			}
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
	private static <T extends DynamicRoot> T getRootByRemote(Class<T> objectType, Object id) {
		var data = RemoteService.getObject(objectType, id);
		return (DynamicRoot) define.createInstance(data);
	}

	/// <summary>
	/// 新增对象到本地仓储
	/// </summary>
	/// <param name="repository"></param>
	/// <param name="define"></param>
	/// <param name="root"></param>
	private static <T extends DynamicRoot> void addRoot(IDynamicRepository repository, Class<T> objectType, T root) {
		repository.add(objectType, root);
		addMemberRoots(repository, root);
	}

	@SuppressWarnings("unchecked")
	private static void addMemberRoots(IDynamicRepository repository, DynamicRoot root) {
		var memberRoots = root.getRefRoots();
		for (var member : memberRoots) {
			var objectType = (Class<? extends DynamicRoot>) member.getClass();

			var id = member.getIdentity();
			// 为了防止死锁，我们开启的是不带锁的模式判定是否有本地数据
			// 虽然这有可能造成重复输入的插入而导致报错，但是几率非常低，而且不会引起灾难性bug
			var local = repository.find(objectType, id, QueryLevel.None);
			if (local.isEmpty()) {
				repository.add(objectType, member);
			}
		}
	}

	/// <summary>
	/// 修改远程对象在本地的映射
	/// </summary>
	/// <param name="define"></param>
	/// <param name="id"></param>
	internal

	static void UpdateObject(AggregateRootDefine define, object id)
	{
	    //这里需要注意，我们不能简单的删除本地对象再等待下次访问时加载
	    //因为对象缓存的原因，对象的属性可能引用了已删除的本地对象
	    //这些被引用的以删除的本地对象只有在对象缓存过期后才更新，导致数据更新不及时
	    //因此需要手工更改对象的内容
	    var repository = Repository.Create<IDynamicRepository>();

	    DataContext.UseTransactionScope(() =>
	    {
	        var local = repository.Find(define, id, QueryLevel.HoldSingle);
	        if (local.IsEmpty()) return; //本地没有数据，不需要更新

	        var root = GetRootByRemote(define, id);
	        if (root.IsEmpty())
	        {
	            DeleteObject(define, id);
	            return;
	        }
	        local.Sync(root); //同步数据
	        UpdateRoot(repository, define, local);
	    });
	}

	private static void UpdateRoot(IDynamicRepository repository, AggregateRootDefine define, DynamicRoot root) {
		repository.Update(define, root); // 保存修改后的数据
		AddMemberRoots(repository, define, root); // 有可能修改后的数据包含新的根成员需要增加
	}

	/// <summary>
	/// 删除远程对象在本地的映射
	/// </summary>
	/// <param name="define"></param>
	/// <param name="id"></param>
	internal

	static void DeleteObject(AggregateRootDefine define, object id)
	{
	    var repository = Repository.Create<IDynamicRepository>();
	    var root = repository.Find(define, id, QueryLevel.Single);
	    if (!root.IsEmpty())
	    {
	        if (define.KeepSnapshot)
	        {
	            //保留快照
	            root.MarkSnapshot();
	            repository.Update(define, root);
	        }
	        else
	        {
	            //同步删除
	            repository.Delete(define, root);
	        }
	        
	    }
	}

	#endregion

	#

	region 广播消息

	/// <summary>
	/// 通知对象已修改
	/// </summary>
	/// <param name="type"></param>
	/// <param name="id"></param>
	internal

	static void NotifyUpdated(RemoteType type, object id) {
		RemoteService.NotifyUpdated(type, id);
	}

	/// <summary>
	/// 通知对象已删除
	/// </summary>
	/// <param name="type"></param>
	/// <param name="id"></param>
	internal

	static void NotifyDeleted(RemoteType type, object id) {
		RemoteService.NotifyDeleted(type, id);
	}

	#endregion

}
