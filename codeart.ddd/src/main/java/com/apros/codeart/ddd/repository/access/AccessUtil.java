package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.ddd.validation.ASCIIStringValidator;
import com.apros.codeart.ddd.validation.StringLengthValidator;
import com.apros.codeart.runtime.TypeUtil;

public final class AccessUtil {

	private AccessUtil() {
	}

	public static int getMaxLength(PropertyMeta meta) {
		var stringMeta = TypeUtil.as(meta, GeneratedField.StringMeta.class);
		if (stringMeta != null) {
			return stringMeta.maxLength();
		} else {
			var sl = meta.findValidator(StringLengthValidator.class);
			return sl == null ? 0 : sl.max();
		}
	}

	public static boolean isASCIIString(PropertyMeta meta) {
		var stringMeta = TypeUtil.as(meta, GeneratedField.StringMeta.class);
		if (stringMeta != null) {
			return stringMeta.ascii();
		} else {
			return meta.findValidator(ASCIIStringValidator.class) != null;
		}
	}

	public static boolean isId(PropertyMeta meta) {
		return meta.name().equalsIgnoreCase(EntityObject.IdPropertyName);
	}

}
