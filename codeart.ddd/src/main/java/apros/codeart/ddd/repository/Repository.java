package apros.codeart.ddd.repository;

import java.lang.reflect.Method;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IRepository;
import apros.codeart.ddd.dynamic.IDynamicRepository;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
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

	/**
	 * 创建一个仓储对象，同一类型的仓储对象会被缓存
	 * 
	 * @param <T>
	 * @param repositoryInterfaceType
	 * @return
	 */
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
		var repositoryType = RepositoryFactory.getRepositoryByObject(objectType).getClass();

		var method = MethodUtil.resolveByName(repositoryType, methodName);
		if (method == null)
			throw new DomainDrivenException(Language.strings("codeart.ddd", "NoDefineMethodFromRepository",
					repositoryType.getName(), methodName));
		return method;
	}

	/**
	 * 
	 * 根据对象名称获得对应的仓储
	 * 
	 * @param <T>
	 * @param rootTypeName
	 * @return
	 */
	public static IRepository create(String rootTypeName) {
		var objectType = ObjectMetaLoader.get(rootTypeName).objectType();
		return RepositoryFactory.getRepositoryByObject(objectType);
	}

	/**
	 * 
	 * 获得动态类型得仓储
	 * 
	 * @param dynamicTypeName
	 * @return
	 */
	public static IDynamicRepository createDynamic(String dynamicTypeName) {
		return (IDynamicRepository) create(dynamicTypeName);
	}
}
