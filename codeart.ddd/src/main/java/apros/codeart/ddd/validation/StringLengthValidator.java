package apros.codeart.ddd.validation;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.i18n.Language;

public class StringLengthValidator extends PropertyValidatorImpl {

	private StringLengthValidator(StringLength tip) {
		super(tip);
	}

	public int min() {
		return this.getTip(StringLength.class).min();
	}

	public int max() {
		return this.getTip(StringLength.class).max();
	}

	@Override
	protected void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result) {

		if (propertyValue != null) {

			var value = (String) propertyValue;
			var tip = this.getTip(StringLength.class);

			int length = value.length();
			if (length < tip.min())
				result.append(property.name(), ErrorCode,
						Language.strings("codeart.ddd", "StringLengthLessThan", property.call(), tip.min()));
			else if (length > tip.max())
				result.append(property.name(), ErrorCode,
						Language.strings("codeart.ddd", "StringLengthMoreThan", property.call(), tip.max()));
		}

	}

	public static final String ErrorCode = "StringLengthError";
}
