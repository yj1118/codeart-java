package apros.codeart.ddd.validation;

import java.util.regex.Pattern;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.util.StringUtil;

public class EmailValidator extends PropertyValidatorImpl {
	private EmailValidator(Email tip) {
		super(tip);
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
			result.append(property.name(), "email",
					Language.strings("codeart.ddd", "IncorrectEmailFormat", property.call()));

	}

	@Override
	public DTObject getData() {
		// 没有数据可以提供传输时使用
		return null;
	}
}
