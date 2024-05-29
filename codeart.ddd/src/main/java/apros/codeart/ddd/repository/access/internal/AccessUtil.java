package apros.codeart.ddd.repository.access.internal;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.validation.ASCIIStringValidator;
import apros.codeart.ddd.validation.StringLengthValidator;
import apros.codeart.runtime.TypeUtil;

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

	public static int getTimePrecision(PropertyMeta meta) {
//		var stringMeta = TypeUtil.as(meta, GeneratedField.StringMeta.class);
//		if (stringMeta != null) {
//			return stringMeta.maxLength();
//		} else {
//			var sl = meta.findValidator(StringLengthValidator.class);
//			return sl == null ? 0 : sl.max();
//		}
	}

//	/**
//	 * 
//	 * 对仓储化有影响的验证器
//	 * 
//	 * @param validatorAnnType
//	 * @return
//	 */
//	public static boolean hasImpact(Class<? extends Annotation> validatorAnnType) {
//		if (validatorAnnType.equals(StringLengthValidator.class) || validatorAnnType.equals(ASCIIStringValidator.class))
//			return true;
//		return false;
//	}
}
