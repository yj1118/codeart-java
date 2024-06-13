package apros.codeart.ddd.metadata.internal;

import java.util.ArrayList;

import apros.codeart.ddd.IObjectValidator;
import apros.codeart.ddd.ObjectValidator;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccessImpl;

public final class ObjectValidatorUtil {

    public static Iterable<IObjectValidator> getValidators(Class<?> objectType) {
        var ann = objectType.getAnnotation(ObjectValidator.class);
        if (ann == null)
            return ListUtil.empty();

        var types = ann.value();
        ArrayList<IObjectValidator> validators = new ArrayList<IObjectValidator>(types.length);
        for (var validatorType : types) {
            validators.add(createValidator(validatorType));
        }
        return validators;
    }

    private static IObjectValidator createValidator(Class<? extends IObjectValidator> validatorType) {
        return SafeAccessImpl.createSingleton(validatorType);
    }

}
