package apros.codeart.ddd.repository;

import java.util.function.Function;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IRepository;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.Activator;
import apros.codeart.util.LazyIndexer;

class RepositoryFactory {

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
		var type = getRepositoryTypeByRegister(repositoryInterfaceType);
		if (type == null) {
			type = getRepositoryTypeByAgree(repositoryInterfaceType);
		}
		return type;
	}

	private static Class<?> getRepositoryTypeByRegister(Class<?> repositoryInterfaceType) {
		return RepositoryRegistrar.getRepositoryType(repositoryInterfaceType);
	}

	private static Class<?> getRepositoryTypeByAgree(Class<?> repositoryInterfaceType) {
		// 例如：UserSubsytem.IUserRepository的仓储就是UserSubsytem.UserRepository
		var repositoryName = String.format("%s.%s", repositoryInterfaceType.getPackageName(),
				repositoryInterfaceType.getSimpleName().substring(1)); // substring(1) 是移除I
		return Class.forName(repositoryInterfaceType.getModule(), repositoryName);
	}

	/**
	 * 创建一个仓储对象，所有仓储对象都是线程安全的，因此为单例创建
	 * 
	 * @param <T>
	 * @param repositoryInterfaceType
	 * @return
	 */
	public static Object create(Class<?> repositoryInterfaceType) {
		return _cache.apply(repositoryInterfaceType);
	}

	private static Function<Class<?>, Object> _cache = LazyIndexer.init((repositoryInterfaceType) -> {
		var repository = createRepositoryImpl(repositoryInterfaceType);
		if (repository == null)
			throw new DomainDrivenException(
					Language.strings("codeart.ddd", "NotFoundRepository", repositoryInterfaceType.getName()));
		return repository;
	});

	private static Object createRepositoryImpl(Class<?> repositoryInterfaceType) {
		var repository = RepositoryRegistrar.getRepository(repositoryInterfaceType);
		if (repository == null) {
			// 没有在注册里找到，那么根据名称约定
			var repositoryType = getRepositoryTypeByAgree(repositoryInterfaceType);
			repository = Activator.createInstance(repositoryType);
		}
		return repository;
	}

	/**
	 * 注册单例仓储，请确保<paramref name="repository"/>是线程访问安全的
	 * 
	 * @param <T>
	 * @param repositoryInterfaceType
	 * @param repository
	 */
	public static <T extends IRepository> void register(Class<?> repositoryInterfaceType, T repository) {
		RepositoryRegistrar.register(repositoryInterfaceType, repository);
	}
}
