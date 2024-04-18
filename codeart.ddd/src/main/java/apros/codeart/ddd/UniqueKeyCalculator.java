package apros.codeart.ddd;

import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;

public final class UniqueKeyCalculator {

	private UniqueKeyCalculator() {
	}

	public static String getUniqueKey(DomainObject obj) {
		var objectType = obj.getClass();
		var root = TypeUtil.as(obj, IAggregateRoot.class);
		if (root != null)
			return getUniqueKey(objectType, root.getIdentity());

		throw new DomainDrivenException(Language.strings("GetUniqueKeyError", objectType.getName()));
	}

	public static String getUniqueKey(Class<?> objectType, Object id) {
		return String.format("%s+%s", objectType.getSimpleName(), id.toString());
	}
}
