package com.apros.codeart.ddd.validation;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.PropertyValidatorImpl;
import com.apros.codeart.ddd.ValidationResult;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.StringUtil;

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
