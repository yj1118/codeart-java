package apros.codeart.ddd.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IRepositoryBase;
import apros.codeart.ddd.dynamic.IDynamicObject;
import apros.codeart.ddd.repository.access.SqlDynamicRepository;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccessImpl;

class RepositoryFactory {

	public static Class<?> getRepositoryType(Class<?> repositoryInterfaceType) {
		return _getRepositoryType.apply(repositoryInterfaceType);
	}

	/**
	 * 创建一个仓储对象，所有仓储对象都是线程安全的，因此为单例创建
	 * 
	 * @param <T>
	 * @param repositoryInterfaceType
	 * @return
	 */
	public static Object create(Class<?> repositoryInterfaceType) {
		return _getRepositoryInstance.apply(repositoryInterfaceType);
	}

	private final static Map<Class<?>, Object> _registers = new HashMap<Class<?>, Object>();

	private static final Function<Class<?>, Object> _getRepositoryInstance = LazyIndexer
			.init((repositoryInterfaceType) -> {
				var repository = _registers.get(repositoryInterfaceType);
				if (repository != null)
					return repository;
				var instanceType = getRepositoryTypeByAgree(repositoryInterfaceType);
				if (instanceType != null) {
					return SafeAccessImpl.createSingleton(instanceType);
				}
				throw new DomainDrivenException(
						Language.strings("codeart.ddd", "NotFoundRepository", repositoryInterfaceType.getName()));
			});

	/**
	 * 
	 * 按照约定名称得到仓储实例的类型
	 * 
	 * @param repositoryInterfaceType
	 * @return
	 */
	private static Class<?> getRepositoryTypeByAgree(Class<?> repositoryInterfaceType) {
		// 例如：UserSubsytem.IUserRepository的仓储就是UserSubsytem.UserRepository
		// substring(1) 是移除I
		var repositoryName = repositoryInterfaceType.getSimpleName().substring(1);
		return TypeUtil.getClass(repositoryName);
//		return Class.forName(repositoryInterfaceType.getModule(), repositoryName);
	}

	/**
	 * 注册单例仓储，请确保<paramref name="repository"/>是线程访问安全的
	 * 
	 * @param <T>
	 * @param repositoryInterfaceType
	 * @param repository
	 */
	public static <T extends IRepositoryBase> void register(Class<?> repositoryInterfaceType, T repository) {
		SafeAccessImpl.checkUp(repository);
		_registers.put(repositoryInterfaceType, repository);
	}

	private static final Function<Class<?>, Class<?>> _getRepositoryType = LazyIndexer
			.init((repositoryInterfaceType) -> {
				var repository = _registers.get(repositoryInterfaceType);
				if (repository != null)
					return repository.getClass();

				var instanceType = getRepositoryTypeByAgree(repositoryInterfaceType);
				if (instanceType != null) {
					return instanceType;
				}
				throw new DomainDrivenException(
						Language.strings("codeart.ddd", "NotFoundRepository", repositoryInterfaceType.getName()));
			});

	// 通过实体类型得到仓储

	public static IRepositoryBase getRepositoryByObject(Class<?> objectType) {
		return _getRepositoryByObject.apply(objectType);
	}

	private static final Function<Class<?>, IRepositoryBase> _getRepositoryByObject = LazyIndexer.init((objectType) -> {

		for (var p : _registers.entrySet()) {
			var repository = (AbstractRepository<?>) p.getValue();
			if (repository.getRootType() == objectType)
				return repository;
		}

		// 通过约定找
		// 例如：UserSubsytem.IUserRepository的仓储就是UserSubsytem.UserRepository
		var repositoryName = String.format("%sRepository", objectType.getSimpleName());
		if (TypeUtil.exists(repositoryName)) {
			var repositoryType = TypeUtil.getClass(repositoryName);
			return (IRepositoryBase) SafeAccessImpl.createSingleton(repositoryType);
		}

		// 都找不到，那么就判断是否为动态类型，如果是，则返回动态类型的通用仓储
		if (IDynamicObject.class.isAssignableFrom(objectType))
			return new SqlDynamicRepository(objectType.getSimpleName());

		throw new DomainDrivenException(Language.strings("codeart.ddd", "NotFoundRepository", objectType.getName()));
	});

}
