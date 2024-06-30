package apros.codeart.ddd.validation;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
public class ASCIIStringValidator extends PropertyListabelValidator {
    public ASCIIStringValidator(ASCIIString tip) {
        super(tip);
    }

    @Override
    protected void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
                            ScheduledActionType actionType, ValidationResult result) {

        if (propertyValue != null) {
            var value = (String) propertyValue;

            if (!StringUtil.isAscii(value)) {
                result.append(property.name(), ErrorCode, Language.strings("NotASCII", value));
            }
        }

    }

    public static final String ErrorCode = "NotASCII";

    @Override
    public DTObject getData() {
        // 没有数据可以提供传输时使用
        return null;
    }
}
