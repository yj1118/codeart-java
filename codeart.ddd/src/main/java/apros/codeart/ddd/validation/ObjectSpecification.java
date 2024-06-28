package apros.codeart.ddd.validation;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.IObjectValidator;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.TypeMismatchException;

public abstract class ObjectSpecification<T extends IDomainObject> implements IObjectValidator {

    @SuppressWarnings("unchecked")
    public final void validate(IDomainObject obj, ScheduledActionType actionType, ValidationResult result) {
        validateImpl((T) obj, actionType, result);
    }

    protected abstract void validateImpl(T obj, ScheduledActionType actionType, ValidationResult result);

}
