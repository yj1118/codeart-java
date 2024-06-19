package subsystem.account;

import apros.codeart.ddd.ValidationResult;
import apros.codeart.ddd.validation.ObjectSpecification;
import apros.codeart.ddd.validation.ValidatorUtil;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class AuthPlatformSpecification extends ObjectSpecification<AuthPlatform> {
    @Override
    protected void validateImpl(AuthPlatform obj, ValidationResult result) {
        ValidatorUtil.checkPropertyRepeated(obj, AuthPlatform.NameProperty, result);
        ValidatorUtil.checkPropertyRepeated(obj, AuthPlatform.ENProperty, result);
    }
}
