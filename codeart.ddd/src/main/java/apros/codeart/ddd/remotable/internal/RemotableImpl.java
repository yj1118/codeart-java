package apros.codeart.ddd.remotable.internal;

import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.metadata.MetadataLoader;
import apros.codeart.ddd.remotable.Remotable;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;

public final class RemotableImpl {

	private Class<?> _objectType;

	public Class<?> objectType() {
		return _objectType;
	}

	public RemotableImpl(Class<?> objectType) {
		_objectType = objectType;
	}

	public static boolean has(Class<?> objectType) {
		return objectType.getAnnotation(Remotable.class) != null;
	}

	/**
	 * 在程序启动的时候分析所有远程特性，记录信息
	 */
	public static void initialize() {
		var types = MetadataLoader.getDomainTypes();
		for (var objectType : types) {
			if (DomainObject.isEmpty(objectType))
				continue;

			var tip = create(objectType);
			if (tip != null) {
				_tips.add(tip);
			}
		}
	}

	/**
	 * 创建并补全 {@objectType} 定义的远程能力特性，特性会被加到索引表
	 * 
	 * @param objectType
	 * @return
	 */
	private static RemotableImpl create(Class<?> objectType) {
		var tip = objectType.getAnnotation(Remotable.class);
		if (tip != null) {
			return new RemotableImpl(objectType);
		}
		return null;
	}

	private final static ArrayList<RemotableImpl> _tips = new ArrayList<RemotableImpl>();

	/**
	 * 获得当前应用程序定义的所有具备远程能力的特性标签
	 * 
	 * @return
	 */
	public static Iterable<RemotableImpl> getTips() {
		return _tips;
	}

	public static RemotableImpl getTip(Class<?> objectType) {
		return ListUtil.find(_tips, (tip) -> tip.objectType().equals(objectType));
	}

	public static RemotableImpl getTip(String typeName) {
		return _getTip.apply(typeName);
	}

	private static Function<String, RemotableImpl> _getTip = LazyIndexer.init((typeName) -> {

		var result = ListUtil.find(_tips, (tip) -> tip.objectType().getSimpleName().equalsIgnoreCase(typeName));
		if (result == null)
			throw new DomainDrivenException(Language.strings("codeart.ddd", "NotFoundRemotable", typeName));
		return result;
	});

}
