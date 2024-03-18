package com.apros.codeart.ddd;

import java.util.Optional;

public class RepositoryFactory {
//	#region 获取仓储实现的类型

	public static Class<?> getRepositoryType(Class<?> repositoryInterfaceType) {
		var repository = getRepositoryTypeImpl(repositoryInterfaceType);
		if (repository == null)
			throw new DomainDrivenException(String.format("NotFoundRepository", repositoryInterfaceType.getName()));
		return repository;
	}

	/**
	 * 
	 * 获取仓储实现的类型
	 * 
	 * @param repositoryInterfaceType
	 * @return
	 */
	private static Class<?> getRepositoryTypeImpl(Class<?> repositoryInterfaceType) {

		return Optional.ofNullable(getRepositoryTypeByConfig(repositoryInterfaceType)).orElseGet(() -> {
			return getRepositoryTypeByRegister(repositoryInterfaceType);
		});

//	    return getRepositoryTypeByConfig(repositoryInterfaceType)
//	            ?? getRepositoryTypeByRegister(repositoryInterfaceType);
	}

	private static Class<?> getRepositoryTypeByConfig(Class<?> repositoryInterfaceType) {
		var config = DomainDrivenConfiguration.Current.RepositoryConfig;
		if (config == null)
			return null;
		var mapper = config.RepositoryMapper;
		if (mapper == null)
			return null;
		return mapper.GetInstanceType(repositoryInterfaceType);
	}

	private static Class<?> getRepositoryTypeByRegister(Class<?> repositoryInterfaceType) {
		return RepositoryRegistrar.GetRepositoryType(repositoryInterfaceType);
	}

	#endregion

	/// <summary>
	/// 创建一个仓储对象，所有仓储对象都是线程安全的，因此为单例创建
	/// </summary>
	/// <typeparam name="TRepository"></typeparam>
	/// <typeparam name="TObject"></typeparam>
	/// <returns></returns>
	public static TRepository Create<TRepository>()
	where TRepository:class,IRepository
	{
	    var repositoryInterfaceType = typeof(TRepository);
	    object repository = _cache.Get(repositoryInterfaceType, (t) =>
	    {
	        repository = CreateRepositoryImpl<TRepository>();
	        if (repository == null)
	            throw new DomainDrivenException(string.Format(Strings.NotFoundRepository, repositoryInterfaceType.FullName));
	        return repository;
	    });
	    return (TRepository)repository;
	}

	public static IRepository Create(Type repositoryInterfaceType)
	{
	    object repository = _cache.Get(repositoryInterfaceType, (t) =>
	    {
	        var repositoryType = GetRepositoryType(repositoryInterfaceType);
	        return SafeAccessAttribute.CreateSingleton(repositoryType);
	    });
	    return (IRepository)repository;
	}

	private static LazyIndexer<Type, object> _cache = new LazyIndexer<Type, object>();

	#region 直接根据仓储配置得到实现

	private static TRepository CreateRepositoryImpl<TRepository>()
	where TRepository:class,IRepository
	{
	    return GetRepositoryByConfig<TRepository>() ?? GetRepositoryByRegister<TRepository>();
	}

	private static TRepository GetRepositoryByConfig<TRepository>()
	where TRepository:class,IRepository
	{
	    var config = DomainDrivenConfiguration.Current.RepositoryConfig;
	    if (config == null) return null;
	    var mapper = config.RepositoryMapper;
	    if (mapper == null) return null;
	    return mapper.GetInstance<TRepository>();
	}

	private static TRepository GetRepositoryByRegister<TRepository>()
	where TRepository:class,IRepository
	{
	    return RepositoryRegistrar.GetRepository(typeof(TRepository)) as TRepository;
	}

	/// <summary>
	/// 注册单例仓储，请确保<paramref name="repository"/>是线程访问安全的
	/// </summary>
	/// <typeparam name="TRepository"></typeparam>
	/// <typeparam name="TObject"></typeparam>
	/// <param name="repository"></param>
	public static void Register<TRepository>(IRepository repository)
	where TRepository:IRepository
	{
	    RepositoryRegistrar.Register<TRepository>(repository);
	}

//	#endregion
}
