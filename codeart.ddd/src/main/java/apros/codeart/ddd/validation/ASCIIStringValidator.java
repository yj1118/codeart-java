package apros.codeart.ddd.validation;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.i18n.Language;
import apros.codeart.util.StringUtil;

public class ASCIIStringValidator extends PropertyValidatorImpl {
	private ASCIIStringValidator(ASCIIString tip) {
		super(tip);
	}

	@Override
	protected void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result) {

		if (propertyValue != null) {
			var value = (String) propertyValue;

			if (!StringUtil.isAscii(value)) {
				result.append(property.name(), ErrorCode, Language.strings("NotASCII", value));
			}
		}

	}

	public static final String ErrorCode = "NotASCII";
}
