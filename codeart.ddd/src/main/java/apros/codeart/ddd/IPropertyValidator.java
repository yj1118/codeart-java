package apros.codeart.ddd;

import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.dto.DTObject;

public interface IPropertyValidator {
    void validate(IDomainObject domainObject, DomainProperty property, ScheduledActionType actionType, ValidationResult result);

    /**
     * 验证器的dto格式数据
     *
     * @return
     */
    DTObject getData();

}
