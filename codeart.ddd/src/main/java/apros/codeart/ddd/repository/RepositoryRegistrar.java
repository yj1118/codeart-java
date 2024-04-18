package apros.codeart.ddd.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IRepository;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccessImpl;
import apros.codeart.util.TypeMismatchException;

/**
 * 通过注册注入的仓储实现
 */
final class RepositoryRegistrar {

	private RepositoryRegistrar() {
	}

//	#region 获取仓储实现的类型

	public static Class<?> getRepositoryType(Class<?> repositoryInterfaceType) {
		return _getGetRepositoryType.apply(repositoryInterfaceType);
	}

	private static Function<Class<?>, Class<?>> _getGetRepositoryType = LazyIndexer.init((repositoryInterfaceType) -> {
		var instance = getRepository(repositoryInterfaceType);
		if (instance == null)
			return null;
		return instance.getClass();
	});

	// #endregion

	public static Object getRepository(Class<?> repositoryInterfaceType) {
		return _singletons.get(repositoryInterfaceType);
	}

	private static Map<Class<?>, Object> _singletons = new HashMap<Class<?>, Object>();

	private static Object _syncObject = new Object();

	/**
	 * 
	 * 注册单例仓储，请确保 repository 是单例的
	 * 
	 * @param <T>
	 * @param repositoryInterfaceType
	 * @param repository
	 */
	public static <T extends IRepository> void register(Class<?> repositoryInterfaceType, T repository) {
		var interfaceType = repositoryInterfaceType;
		if (_singletons.containsKey(interfaceType))
			throw new DomainDrivenException(
					Language.strings("codeart.ddd", "RepeateRepository", repositoryInterfaceType.getName()));
		synchronized (_syncObject) {
			if (_singletons.containsKey(interfaceType))
				throw new DomainDrivenException(
						Language.strings("codeart.ddd", "RepeateRepository", repositoryInterfaceType.getName()));
			if (!interfaceType.isInstance(repository))
				throw new TypeMismatchException(interfaceType, repository.getClass());
			SafeAccessImpl.checkUp(repository);
			_singletons.put(interfaceType, repository);
		}
	}
}
