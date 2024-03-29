package com.apros.codeart.ddd;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.SafeAccessAnn;

public abstract class PropertyValidatorImpl implements IPropertyValidator {

	public void validate(IDomainObject domainObject, DomainProperty property, ValidationResult result) {
		var obj = (DomainObject) domainObject;
		var pro = (DomainProperty) property;
		var propertyValue = obj.getValue(pro);
		validate(obj, pro, propertyValue, result);
	}

	protected abstract void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result);

	public static ArrayList<IPropertyValidator> getValidators(Iterable<Annotation> anns) {
		ArrayList<IPropertyValidator> validators = new ArrayList<IPropertyValidator>();

		fillAnnotationValidators(anns, validators);
		fillClassValidators(anns, validators);

		return validators;
	}

	/**
	 * 获得注解验证器
	 * 
	 * @param anns
	 * @return
	 */
	private static void fillAnnotationValidators(Iterable<Annotation> anns, ArrayList<IPropertyValidator> validators) {

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
	}

	/**
	 * 获取通过 {@PropertyValidator} 标签定义得验证器
	 * 
	 * @param anns
	 * @param validators
	 * @return
	 */
	private static void fillClassValidators(Iterable<Annotation> anns, ArrayList<IPropertyValidator> validators) {
		var ann = TypeUtil.as(ListUtil.find(anns, (a) -> a.annotationType().equals(PropertyValidator.class)),
				PropertyValidator.class);
		if (ann == null)
			return;

		var types = ann.value();
		for (var validatorType : types) {
			validators.add(createValidator(validatorType));
		}
	}

	private static IPropertyValidator createValidator(Class<? extends IPropertyValidator> validatorType) {
		return SafeAccessAnn.createSingleton(validatorType);
	}

}