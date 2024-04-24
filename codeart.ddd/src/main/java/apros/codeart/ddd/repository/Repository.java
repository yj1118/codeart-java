package apros.codeart.ddd.repository;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.google.common.collect.Iterables;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IRepository;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.remotable.internal.RemotePortal;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.MethodUtil;
import apros.codeart.util.StringUtil;

public final class Repository {

	private Repository() {
	}

	/**
	 * 注册仓储，请确保<paramref name="repository"/>是线程访问安全的
	 * 
	 * @param <T>
	 * @param repositoryInterfaceType
	 * @param repository
	 */
	public static <T extends IRepository> void register(Class<?> repositoryInterfaceType, T repository) {
		RepositoryFactory.register(repositoryInterfaceType, repository);
	}

	/// <summary>
	/// 创建一个仓储对象，同一类型的仓储对象会被缓存
	/// </summary>
	/// <typeparam name="TRepository"></typeparam>
	/// <returns></returns>
	@SuppressWarnings("unchecked")
	public static <T extends IRepository> T create(Class<T> repositoryInterfaceType) {
		return (T) RepositoryFactory.create(repositoryInterfaceType);
	}

	public static IRepository createByObjectType(Class<?> objectType) {
		var objectTip = ObjectRepositoryImpl.getTip(objectType, false);
		if (objectTip == null)
			return null;
		return (IRepository) RepositoryFactory.create(objectTip.repositoryInterfaceType());
	}

	/**
	 * 得到对象类型对应的仓储上定义的方法
	 * 
	 * @param objectType
	 * @param methodName
	 * @return
	 */
	public static Method getMethodFromRepository(Class<?> objectType, String methodName) {
		if (StringUtil.isNullOrEmpty(methodName))
			return null;
		var objectTip = ObjectRepositoryImpl.getTip(objectType, true);
		var repositoryType = RepositoryFactory.getRepositoryType(objectTip.repositoryInterfaceType());

		var method = MethodUtil.resolveByName(repositoryType, methodName);
		if (method == null)
			throw new DomainDrivenException(Language.strings("codeart.ddd", "NoDefineMethodFromRepository",
					repositoryType.getName(), methodName));
		return method;
	}

//	#region 远程对象

	/// <summary>
	/// 查找远程根对象
	/// </summary>
	/// <param name="id"></param>
	/// <returns></returns>
	public static <T extends DynamicRoot> T findRemoteRoot(Class<T> objectType, Object id) {
		return RemotePortal.getObject(objectType, id, QueryLevel.None);
	}

	public static <T extends DynamicRoot> T findRemoteRootWithLock(Class<T> objectType, Object id) {
		return RemotePortal.getObject(objectType, id, QueryLevel.Single);
	}

	public static <T extends DynamicRoot> Iterable<T> findRemoteRoots(Class<T> objectType, Iterable<Object> ids) {
		var items = new ArrayList<T>(Iterables.size(ids));

		for (var id : ids) {
			var item = Repository.findRemoteRoot(objectType, id);
			items.add(item);
		}
		return items;
	}

//	#endregion

}
