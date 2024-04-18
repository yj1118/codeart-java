package apros.codeart.ddd;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Function;

import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.runtime.Util;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

public final class ConstructorParameterInfo {

	private Parameter _original;

	public Parameter original() {
		return _original;
	}

	private int _index;

	/**
	 * 参数的序号
	 * 
	 * @return
	 */
	public int index() {
		return _index;
	}

	public String name() {
		return _original.getName();
	}

	private PropertyMeta _propertyTip;

	/**
	 * 
	 * 获取这个参数对应领域对象的属性的仓储定义，如果不为空，可以自动生成加载代码
	 * 
	 * @return
	 */
	public PropertyMeta propertyTip() {
		return _propertyTip;
	}

	public DomainPropertyCategory propertyCategory() {
		return this.propertyTip().category();
	}

	private ParameterRepository _tip;

	public ParameterRepository tip() {
		return _tip;
	}

	public Class<?> implementType() {
		if (_tip == null)
			return null;
		return _tip.implementType();
	}

	public Class<?> declaringType() {
		return _constructorTip.constructor().getDeclaringClass();
	}

	private ConstructorRepositoryImpl _constructorTip;
	private Method _loadData = null;

	/**
	 * 获取运行时类型定义的加载方法，这常用于派生类定义的仓储实现
	 */
	private Function<Class<?>, Method> _getLoadData;

	public ConstructorParameterInfo(ConstructorRepositoryImpl constructorTip, Parameter original) {
		_constructorTip = constructorTip;
		_original = original;
		_tip = original.getAnnotation(ParameterRepository.class);
		_propertyTip = getPropertyTip();
		_getLoadData = LazyIndexer.init((objectType) -> {
			if (this.tip() == null || StringUtil.isNullOrEmpty(this.tip().loadMethod()))
				return null;
			return Repository.getMethodFromRepository(objectType, this.tip().loadMethod());
		});
	}

	private PropertyMeta getPropertyTip() {
		ObjectMeta objectTip = _constructorTip.objectTip();
		var propertyName = this.original().getName();
		return objectTip.findProperty(propertyName);
	}

	/**
	 * 
	 * 使用自定义方法加载参数数据
	 * 
	 * @param objectType
	 * @param data
	 * @param level
	 * @param object
	 * @return
	 */
	public Object loadData(Class<?> objectType, MapData data, QueryLevel level) {

		try {
			var method = _getLoadData.apply(objectType);
			if (method == null)
				method = _loadData;
			if (method == null)
				return null;
			return method.invoke(null, data, level);
		} catch (Exception e) {
			throw Util.propagate(e);
		}
	}
}