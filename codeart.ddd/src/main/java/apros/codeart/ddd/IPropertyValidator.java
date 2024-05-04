package apros.codeart.ddd;

import apros.codeart.dto.DTObject;

public interface IPropertyValidator {
	void validate(IDomainObject domainObject, DomainProperty property, ValidationResult result);

	/**
	 * 
	 * 验证器的dto格式数据
	 * 
	 * @return
	 */
	DTObject getData();

}
