package apros.codeart.ddd.repository;

import java.util.function.Function;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;

public class ObjectRepositoryImpl {

	/**
	 * 该对象所用到的仓储接口的类型
	 */
	private Class<?> _repositoryInterfaceType;

	public Class<?> repositoryInterfaceType() {
		return _repositoryInterfaceType;
	}

	private Class<?> _objectType;

	public Class<?> objectType() {
		return _objectType;
	}

	void objectType(Class<?> value) {
		_objectType = value;
	}

	/**
	 * 关闭多租户功能，这意味着即使配置文件开启了多租户特性，目标对象也不会启动多租户功能
	 */
	private boolean _closeMultiTenancy;

	public boolean closeMultiTenancy() {
		return _closeMultiTenancy;
	}

	public ObjectRepositoryImpl(Class<?> objectType, Class<?> repositoryInterfaceType, boolean closeMultiTenancy) {
		_objectType = objectType;
		_repositoryInterfaceType = repositoryInterfaceType;
		_closeMultiTenancy = closeMultiTenancy;
	}

	public static ObjectRepositoryImpl getTip(Class<?> objectType, boolean checkUp) {
		var attr = _getTip.apply(objectType);
		if (attr == null && checkUp)
			throw new DomainDrivenException(
					Language.strings("codeart.ddd", "NotDefinedObjectRepository", objectType.getName()));
		return attr;
	}

	private static Function<Class<?>, ObjectRepositoryImpl> _getTip = LazyIndexer.init((objectType) -> {

		ObjectRepository annon = objectType.getAnnotation(ObjectRepository.class);

		if (annon == null)
			return null;

		return new ObjectRepositoryImpl(objectType, annon.value(), annon.closeMultiTenancy());
	});
}
