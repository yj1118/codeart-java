package com.apros.codeart.ddd;

import java.lang.reflect.Method;

import com.apros.codeart.runtime.MethodUtil;
import com.apros.codeart.util.StringUtil;
import com.google.common.base.Strings;

public class Repository {

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
	public static <T extends IRepository> T create(Class<?> repositoryInterfaceType) {
		return (T) RepositoryFactory.create(repositoryInterfaceType);
	}

	static IRepository createBy(Class<?> objectType) {
		var objectTip = ObjectRepositoryAnn.getTip(objectType, false);
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
	static Method getMethodFromRepository(Class<?> objectType, String methodName) {
		if (StringUtil.isNullOrEmpty(methodName))
			return null;
		var objectTip = ObjectRepositoryAnn.getTip(objectType, true);
		var repositoryType = RepositoryFactory.getRepositoryType(objectTip.repositoryInterfaceType());

		var method = MethodUtil.resolveByNameMemoized(repositoryType, methodName);
		if (method == null)
			throw new DomainDrivenException(
					String.format(Strings.NoDefineMethodFromRepository, repositoryType.FullName, methodName));
		return method;
	}

//	#region 远程对象
//
//	/// <summary>
//	/// 查找远程根对象（使用动态对象须引用Microsoft.CSharp）
//	/// </summary>
//	/// <param name="id"></param>
//	/// <returns></returns>
//	public static dynamic FindRemoteRoot<T>(object id)
//	where T:AggregateRootDefine
//	{
//	    var define =(AggregateRootDefine)TypeDefine.GetDefine<T>();
//	    return RemotePortal.GetObject(define, id, QueryLevel.None);
//	}
//
//	public static dynamic FindRemoteRootWithLock<T>(object id)
//	where T:AggregateRootDefine
//	{
//	    var define = (AggregateRootDefine)TypeDefine.GetDefine<T>();
//	    return RemotePortal.GetObject(define, id, QueryLevel.Single);
//	}
//
//	public static IEnumerable<dynamic> FindRemoteRoots<T>(IEnumerable<object> ids)
//	where T:AggregateRootDefine
//	{
//	    var items = new List<dynamic>(ids.Count());
//	    foreach (var id in ids)
//	    {
//	        var item = Repository.FindRemoteRoot<T>(id);
//	        items.Add(item);
//	    }
//	    return items;
//	}

//	#endregion

}
