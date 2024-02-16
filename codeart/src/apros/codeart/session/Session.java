package apros.codeart.session;

import java.util.function.Function;

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
	        if (useSymbiosis) Symbiosis.Open();
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

	public static <T> T getOrAddItem(String name, Function<T> factory) {
		var session = Current;
		Object item = session.getItem(name);
		if (item == null) {
			item = factory();
			appSession.SetItem(name, item);
		}
		return (T) item;
	}

	public static <T> void setItem(String name, T value) {
		Current.SetItem(name, value);
	}

	public static object GetItem(string name) {
		return Current.GetItem(name);
	}

	public static T GetItem<T>(
	string name)
	{
		return (T) GetItem(name);
	}

}
