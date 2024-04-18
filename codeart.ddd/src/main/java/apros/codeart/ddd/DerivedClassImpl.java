package apros.codeart.ddd;

import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public final class DerivedClassImpl {

	private String _typeKey;

	public String typeKey() {
		return _typeKey;
	}

	private Class<?> _derivedType;

	public Class<?> derivedType() {
		return _derivedType;
	}

	/// <summary>
	///
	/// </summary>
	/// <param name="derivedType"></param>
	/// <param name="typeKey"></param>

	/**
	 * @param derivedType 派生类类型
	 * @param typeKey     派生类类型对应的唯一标识符，这一般是一个GUID
	 */
	public DerivedClassImpl(Class<?> derivedType, String typeKey) {
		_derivedType = derivedType;
		_typeKey = typeKey;
	}

	private static ArrayList<DerivedClassImpl> _impls = new ArrayList<>();

	public static Class<?> getDerivedType(String typeKey) {
		if (StringUtil.isNullOrEmpty(typeKey))
			return null;
		var p = ListUtil.find(_impls, (impl) -> impl.typeKey().equals(typeKey));
		if (p != null)
			return p.derivedType();
		throw new DomainDrivenException(Language.strings("codeart.ddd", "NotFoundDerivedType", typeKey));
	}

	public static Iterable<Class<?>> getDerivedTypes(Class<?> objectType) {
		return _getDerivedTypes.apply(objectType);
	}

	private static Function<Class<?>, Iterable<Class<?>>> _getDerivedTypes = LazyIndexer.init((objectType) -> {
		ArrayList<Class<?>> types = new ArrayList<Class<?>>();
		for (var impl : _impls) {
			var type = impl.derivedType();
			if (type.isAssignableFrom(objectType))
				types.add(type);
		}
		return types;
	});

	private static DerivedClass getAttribute(Class<?> type) {
		return type.getAnnotation(DerivedClass.class);
	}

	/**
	 * 
	 * 检查类型是否标记正确的派生类标签
	 * 
	 * @param domainObjectType
	 */
	public static void checkUp(Class<?> domainObjectType) {
		if (isDerived(domainObjectType)) {
			var attr = getAttribute(domainObjectType);
			if (attr == null || StringUtil.isNullOrEmpty(attr.value()))
				throw new DomainDrivenException(
						Language.strings("codeart.ddd", "NeedTypeKey", domainObjectType.getName()));
		}
	}

	/// <summary>
	/// 类型<paramref name="domainObjectType"/>是否为另外一个领域类型的派生类
	/// </summary>
	/// <param name="domainObjectType"></param>
	/// <returns></returns>
	public static boolean isDerived(Class<?> domainObjectType) {
		if (domainObjectType == null)
			return false;
		if (!ObjectMeta.isDomainObject(domainObjectType))
			return false;

		var baseType = domainObjectType.getSuperclass();
		return baseType != null && !ObjectMeta.isMergeDomainType(baseType);
	}

	public static void init(Class<?> domainType) {
		var attr = getAttribute(domainType);
		if (attr != null) {
			var typeKey = attr.value();
			var impl = new DerivedClassImpl(domainType, typeKey);
			_impls.add(impl);
		}
	}

}
