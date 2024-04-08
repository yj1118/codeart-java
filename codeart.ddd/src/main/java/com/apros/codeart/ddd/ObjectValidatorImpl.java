package com.apros.codeart.ddd;

import java.util.ArrayList;

import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.SafeAccessImpl;
import com.apros.codeart.util.TypeMismatchException;

public abstract class ObjectValidatorImpl implements IObjectValidator {

	/**
	 * 
	 * 提供给基类使用的工具方法
	 * 
	 * @param <T>
	 * @param obj
	 * @param doClass
	 * @return
	 */
	protected <T extends IDomainObject> T asObject(IDomainObject obj, Class<T> doClass) {
		T target = TypeUtil.as(obj, doClass);
		if (target == null)
			throw new TypeMismatchException(obj.getClass(), doClass);

		return target;
	}

	public abstract void validate(IDomainObject obj, ValidationResult result);

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
