package com.apros.codeart.ddd;

import java.util.ArrayList;

import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.SafeAccessAnn;

public abstract class PropertyValidator implements IPropertyValidator {

	public void validate(IDomainObject domainObject, IDomainProperty property, ValidationResult result) {
		var obj = (DomainObject) domainObject;
		var pro = (DomainProperty) property;
		var propertyValue = obj.getValue(pro);
		validate(obj, pro, propertyValue, result);
	}

	protected abstract void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result);

	/**
	 * @param declaringType
	 * @param propertyName
	 * @return
	 */
	static Iterable<IPropertyValidator> getValidators(Class<?> declaringType, String propertyName) {
		var anns = DomainProperty.getAnnotations(declaringType, propertyName);

		ArrayList<IPropertyValidator> validators = new ArrayList<>();

		// 这里的规则是：
		// 在领域属性上定义了的所有注解中，只要对应的注解上有 xxValidator的类，那么就是属性验证器
		// 例如： Email 注解并且同时存在EmailValidator，那么我们就认为该属性需要通过EmailValidator验证

		for (var ann : anns) {
			var annName = ann.annotationType().getSimpleName();

			var validatorType = TypeUtil.getClass(String.format("%sValidator", annName),
					ann.annotationType().getClassLoader());
			if (validatorType == null)
				continue;

			var validator = SafeAccessAnn.createSingleton(validatorType);
			validators.add((IPropertyValidator) validator);
		}

		return validators;
	}

}