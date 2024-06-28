package apros.codeart.ddd;

import apros.codeart.ddd.repository.ScheduledActionType;

public interface IObjectValidator {
    void validate(IDomainObject domainObject, ScheduledActionType actionType, ValidationResult result);
}
