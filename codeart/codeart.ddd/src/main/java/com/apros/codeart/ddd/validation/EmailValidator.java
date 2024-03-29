package com.apros.codeart.ddd.validation;

import java.util.regex.Pattern;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.PropertyValidatorImpl;
import com.apros.codeart.ddd.ValidationResult;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.StringUtil;

public class EmailValidator extends PropertyValidatorImpl {
	private EmailValidator() {
	}

	private static Pattern pattern = Pattern
			.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

	public static boolean isMatch(String input) {
		return pattern.matcher(input).matches();
	}

	@Override
	protected void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result) {
		var value = (String) propertyValue;
		if (StringUtil.isNullOrEmpty(value))
			return; // 是否能为空的验证由别的验证器处理

		if (!isMatch(value))
			result.append(property.getName(), "email", Language.strings("IncorrectEmailFormat", property.call()));

	}

	public static final EmailValidator Instance = new EmailValidator();

}
