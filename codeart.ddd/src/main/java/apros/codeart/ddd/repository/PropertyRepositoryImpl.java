package apros.codeart.ddd.repository;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;

import apros.codeart.ddd.IPropertyDataLoader;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.util.StringUtil;

public class PropertyRepositoryImpl {

	private boolean _lazy;

	public boolean lazy() {
		return _lazy;
	}

	private IPropertyDataLoader _loader;

	public IPropertyDataLoader loader() {
		return _loader;
	}

	public PropertyRepositoryImpl(PropertyRepository ann, Class<?> objectType) {
		_lazy = ann.lazy();
		_loader = getLoader(objectType, ann.loadMethod());
	}

	private PropertyRepositoryImpl() {
		_lazy = false;
		_loader = null;
	}

	private IPropertyDataLoader getLoader(Class<?> objectType, String loadMethod) {
		if (StringUtil.isNullOrEmpty(loadMethod))
			return null;
		var method = Repository.getMethodFromRepository(objectType, loadMethod);
		if (method == null)
			return null;
		return (data, level) -> {
			return loadData(method, data, level);
		};
	}

	/**
	 * 使用自定义方法加载参数数据
	 * 
	 * @param objectType
	 * @param data
	 * @param level
	 * @return
	 */
	private static Object loadData(Method method, MapData data, QueryLevel level) {
		try {
			var args = new Object[2];
			args[0] = data;
			args[1] = level;
			return method.invoke(null, args);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	public static final PropertyRepositoryImpl Default = new PropertyRepositoryImpl();

}
