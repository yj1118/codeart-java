package apros.codeart.session;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 应用程序会话，指的是在应用程序执行期间，不同的用户会拥有自己的appSession，该对象仅对当前用户负责
 * 不会有并发冲突，该对象内部的数据是当前用户独享的
 */
public final class Session {
	private Session() {
	}

	/// <summary>
	///
	/// </summary>
	public static void using(Runnable action)
	{
	    try
	    {
	        Initialize();
	        action();
	    }
	    catch (Exception)
	    {
	        throw;
	    }
	    finally
	    {
	        if (useSymbiosis) Symbiosis.Close();
	        Dispose();
	    }
	}

	private static ISession _current;

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

	private static ISession current() {
		return ThreadSession.instance;
	}

	/**
	 * 是否存在回话
	 * 
	 * @return
	 */
	public static boolean exists() {
		return current() != null && current().valid();
	}

	@SuppressWarnings("unchecked")
	public static <T> T obtainItem(String name, Supplier<T> factory) {
		var session = current();
		Object item = session.getItem(name);
		if (item == null) {
			item = factory.get();
			session.setItem(name, item);
		}
		return (T) item;
	}

	public static <T> void setItem(String name, T value) {
		current().setItem(name, value);
	}

//	public static Object getItem(String name) {
//		return current().getItem(name);
//	}

	@SuppressWarnings("unchecked")
	public static <T> T getItem(String name) {
		return (T) current().getItem(name);
	}

}
