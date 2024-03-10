package com.apros.codeart.context;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.function.Supplier;

import com.apros.codeart.pooling.Pool;
import com.apros.codeart.runtime.MethodUtil;

/**
 * 应用程序会话，指的是在应用程序执行期间，不同的请求会拥有自己的appSession，该对象仅对当前用户负责
 * 不会有并发冲突，该对象内部的数据是当前用户独享的
 */
public final class ContextSession {
	private ContextSession() {
	}

	/**
	 * 使用会话
	 * 
	 * @param action
	 * @throws Exception
	 */
	public static void using(Runnable action) {
		try {
			initialize();
			action.run();
		} catch (Exception ex) {
			throw propagate(ex);
		} finally {
			dispose();
		}
	}

	private static void initialize() throws Exception {
		if (exists())
			return; // 当前回话已存在，不用初始化
		getCurrent().initialize();
		process_start();
	}

	/// <summary>
	/// 释放当前回话
	/// </summary>
	private static void dispose() {
		process_end();
		getCurrent().clear();
	}

	private static void process_start() {
		try {
			for (var method : _startHandles)
				method.invoke(null);
		} catch (Exception ex) {
			throw propagate(ex);
		} finally {
			dispose();
		}
	}

	private static void process_end() {
		try {
			for (var method : _endHandles)
				method.invoke(null);
		} catch (Exception ex) {
			throw propagate(ex);
		} finally {
			dispose();
		}
	}

	private static HashSet<Method> _startHandles = new HashSet<Method>();
	private static HashSet<Method> _endHandles = new HashSet<Method>();

	public static void registerStartHandle(String fullMethodName)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		_startHandles.add(MethodUtil.get(fullMethodName));
	}

	public static void registerEndHandle(String fullMethodName)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		_endHandles.add(MethodUtil.get(fullMethodName));
	}

//	private static IAppSession _current;

//	private static IAppSession Current
//	{
//	    get
//	    {
//	        if (_current == null)
//	        {
//	            _current = _sessionByConfig ?? _sessionByRegister ?? ThreadSession.Instance;
//	        }
//	        return _current;
//	    }
//	}

	private static IContextSession getCurrent() {
		return ThreadSession.instance;
	}

	/**
	 * 是否存在回话
	 * 
	 * @return
	 */
	public static boolean exists() {
		return getCurrent() != null && getCurrent().valid();
	}

	public static boolean containsItem(String name) {
		return exists() && getCurrent().containsItem(name);
	}

	@SuppressWarnings("unchecked")
	public static <T> T obtainItem(String name, Supplier<T> factory) {
		if (!exists())
			return factory.get();
		var session = getCurrent();
		Object item = session.getItem(name);
		if (item == null) {
			item = factory.get();
			session.setItem(name, item);
		}
		return (T) item;
	}

	/**
	 * 
	 * 从池中得到一个项，存入应用程序会话中（名称为name），在应用程序会话有效的期间
	 * 
	 * 会话取出的{name}项目都是同一个实例，
	 * 
	 * 当会话结束后，该项会被归还到池中
	 * 
	 * @param <T>
	 * @param name
	 * @param pool
	 * @return
	 * @throws Exception
	 */
	public static <T> T obtainItem(String name, Pool<T> pool) {
		return obtainItem(name, () -> {
			return pool.borrow();
		}).getItem();
	}

	/**
	 * 
	 * 获得一个对象，该对象会在应用程序会话结束后释放（要么清理资源，要么被永久关闭）
	 * 
	 * @param <T>
	 * @param pool
	 * @param factory
	 * @return
	 * @throws Exception
	 */
	public static <T> T obtainItem(Pool<T> pool, Supplier<T> factory) {
		if (!exists()) // 如果不存在回话，那么直接构造
			return factory.get();
		return Symbiosis.obtain(pool);
	}

	/**
	 * 注册项，当会话上下文结束后，会被回收或者清理
	 * 
	 * @param <T>
	 * @param factory
	 * @return
	 */
	public static <T> T registerItem(T obj) {
		Symbiosis.register(obj);
		return obj;
	}

	public static void setItem(String name, Object value) {
		getCurrent().setItem(name, value);
	}

//	public static Object getItem(String name) {
//		return current().getItem(name);
//	}

	@SuppressWarnings("unchecked")
	public static <T> T getItem(String name) {
		return (T) getCurrent().getItem(name);
	}
}
