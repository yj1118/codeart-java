package apros.codeart;

import static apros.codeart.runtime.Util.propagate;

import com.google.common.collect.Iterables;

import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ArgumentAssert;
import apros.codeart.util.ListUtil;
import apros.codeart.util.PrimitiveUtil;
import apros.codeart.util.SafeAccessImpl;
import apros.codeart.util.StringUtil;
import apros.codeart.util.TypeMismatchException;

public final class InterfaceImplementer {

	private Class<?> _implementType;

	/**
	 * 接口的实现类型
	 * 
	 * @return
	 */
	public Class<?> implementType() {
		return _implementType;
	}

	private Object[] _arguments;

	/**
	 * 创建实现对象时需要传递的构造参数
	 * 
	 * @return
	 */
	public Object[] arguments() {
		return _arguments;
	}

	public InterfaceImplementer(Class<?> implementType, Object[] arguments) {
		_implementType = implementType;
		_arguments = arguments;
	}

	public <T> T getInstance(Class<T> interfaceType) {
		var isSafe = SafeAccessImpl.isDefined(interfaceType);
		var obj = isSafe ? getInstanceBySingleton() : createInstance();
		var instance = TypeUtil.as(obj, interfaceType);
		if (instance == null)
			throw new TypeMismatchException(obj.getClass(), interfaceType);
		return instance;
	}

//	/// <summary>
//	/// 不论对象是否为单例，都创造新的实例
//	/// </summary>
//	/// <typeparam name="T"></typeparam>
//	/// <returns></returns>
//	private <T> T createInstance(Class<T> interfaceType) {
//		var obj = createInstance();
//		var instance = TypeUtil.as(obj, interfaceType);
//		if (instance == null)
//			throw new TypeMismatchException(obj.getClass(), interfaceType);
//		return instance;
//	}

	private final Object _syncObject = new Object();
	private volatile Object _singletonInstance = null;

	/**
	 * 以单例的形式得到实例
	 * 
	 * @return
	 */
	private Object getInstanceBySingleton() {
		if (_singletonInstance == null) {
			synchronized (_syncObject) {
				if (_singletonInstance == null) {
					_singletonInstance = createInstance();
				}
			}
		}
		return _singletonInstance;
	}

	private Object createInstance() {
		ArgumentAssert.isNotNull(this.implementType(), "ImplementType");
		var instance = Activator.createInstance(this.implementType(), this.arguments());
		if (instance == null)
			throw new NoTypeDefinedException(this.implementType());
		return instance;
	}

//	#region 静态成员

	/**
	 * 从xml节点中获取定义
	 * 
	 * @param section
	 * @return
	 */
	public static InterfaceImplementer create(DTObject section) {
		if (section == null)
			return null;
		try {
			String implementName = section.getString("type");
			ArgumentAssert.isNotNull(implementName, "type");

			Class<?> implementType = Class.forName(implementName);
			if (implementType == null)
				throw new NoTypeDefinedException(implementName);

			var args = getArguments(section);

			return new InterfaceImplementer(implementType, args);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static Object[] getArguments(DTObject section) {
		var nodes = section.getObjects("arguments", false);
		var nodeLength = Iterables.size(nodes);
		if (nodes == null || nodeLength == 0)
			return ListUtil.emptyObjects();

		Object[] arguments = new Object[nodeLength];
		for (var i = 0; i < nodeLength; i++) {
			var node = Iterables.get(nodes, i);
			var value = node.getString("value", null);
			ArgumentAssert.isNotNull(value, "value");

			var type = node.getString("type", null);
			if (StringUtil.isNullOrEmpty(type))
				type = "string";

			Object arg = PrimitiveUtil.convert(value, type);
			if (arg == null)
				throw new ClassCastException(
						Language.strings("codeart", "OnlySupportedTypes", PrimitiveUtil.PrimitiveTypes));
			arguments[i] = arg;
		}
		return arguments;
	}

//	#endregion

}
