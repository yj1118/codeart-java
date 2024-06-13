package apros.codeart.ddd.validation;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.IObjectValidator;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.TypeMismatchException;

public abstract class ObjectSpecification<T extends IDomainObject> implements IObjectValidator {

    @SuppressWarnings("unchecked")
    public final void validate(IDomainObject obj, ValidationResult result) {
        validateImpl((T) obj, result);
    }

    protected abstract void validateImpl(T obj, ValidationResult result);

}
