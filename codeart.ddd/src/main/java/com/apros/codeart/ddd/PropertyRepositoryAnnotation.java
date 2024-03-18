package com.apros.codeart.ddd;

import java.lang.reflect.Method;
import java.util.function.Function;

import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.LazyIndexer;

class PropertyRepositoryAnnotation {

	/**
	 * 属性信息
	 */
	private DomainProperty _property;

	public DomainProperty getProperty() {
		return _property;
	}

	void setProperty(DomainProperty value) {
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
		initEmptyable(property);
	}

	private boolean _lazy;

	public boolean lazy() {
		return _lazey;
	}

	private String _loadMethod;

	public String loadMethod() {
		return _loadMethod;
	}

	public PropertyRepositoryAnnotation(boolean lazy, String loadMethod) {
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

	/// <summary>
	/// 使用自定义方法加载参数数据
	/// </summary>
	/// <param name="objectType">运行时的实际类型，有可能是派生类的类型</param>
	/// <param name="data"></param>
	/// <param name="value"></param>
	/// <returns></returns>
	public boolean tryLoadData(Type objectType, DynamicData data, QueryLevel level, out object value)
	 {
	     value = null;
	     var method = _getLoadData(objectType);
	     if (method == null) return false;

	using (var temp = ArgsPool.Borrow2())
	     {
	         var args = temp.Item;
	         args[0] = data;
	         args[1] = level;
	         value = method.Invoke(null, args);
	     }
	     return true;
	 }

}
