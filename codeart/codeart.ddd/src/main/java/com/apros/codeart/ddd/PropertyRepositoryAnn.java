package com.apros.codeart.ddd;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.function.Function;

import com.apros.codeart.ddd.metadata.PropertyAccessLevel;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.LazyIndexer;

class PropertyRepositoryAnn {

	/**
	 * 属性信息
	 */
	private DomainProperty _property;

	public DomainProperty getProperty() {
		return _property;
	}

	private void setProperty(DomainProperty value) {
		_property = value;
		initPropertyType(value);
	}

	/// <summary>
	/// 当属性是集合类型时，获取成员的类型（得到dynamic对应的实际类型）
	/// 如果属性不是集合类型，调用该方法无效
	/// </summary>
	public Class<?> getElementType() {
		if (_property.getDynamicType() != null)
			return _property.getDynamicType();

		return TypeUtil.resolveElementType(_property.getPropertyType());
	}

	public Class<?> getPropertyType() {
		return _property.getPropertyType();
	}

	public boolean isPublicSet() {
		return _property.accessLevelSet() == PropertyAccessLevel.Public;
	}

	DomainPropertyType getDomainPropertyType() {
		return _property.getDomainPropertyType();
	}

	public String getPropertyName() {
		return _property.getName();
	}

	public Class<?> getDeclaringType() {
		return _property.getDeclaringType();
	}

	private String _path;

	public String getPath() {
		return _path;
	}

	private void initPropertyType(DomainProperty property) {
		_path = String.format("%s.%s", property.getDeclaringType().getName(), property.getName());
	}

	private boolean _lazy;

	public boolean lazy() {
		return _lazy;
	}

	private String _loadMethod;

	public String loadMethod() {
		return _loadMethod;
	}

	public PropertyRepositoryAnn(PropertyRepository ann, DomainProperty property) {
		_lazy = ann.lazy();
		_loadMethod = ann.loadMethod();
		setProperty(property);
		initDataAction();
	}

//	#region 自定义加载和保存数据

	/// <summary>
	/// 获取运行时类型定义的加载方法，这常用于派生类定义的仓储实现
	/// </summary>
	private Function<Class<?>, Method> _getLoadData;

	private void initDataAction() {
		_getLoadData = LazyIndexer.init((objectType) -> {
			return Repository.getMethodFromRepository(objectType, this.loadMethod());
		});
	}

	/**
	 * 使用自定义方法加载参数数据
	 * 
	 * @param objectType
	 * @param data
	 * @param level
	 * @return
	 */
	public Object loadData(Class<?> objectType, DynamicData data, QueryLevel level) {
		try {
			var method = _getLoadData.apply(objectType);
			if (method == null)
				return null;
			var args = new Object[2];
			args[0] = data;
			args[1] = level;
			return method.invoke(null, args);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

}
