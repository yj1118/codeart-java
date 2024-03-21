package com.apros.codeart.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class PropertyValidator<TValue> implements IPropertyValidator {

	public void validate(IDomainObject domainObject, IDomainProperty property, ValidationResult result) {
		var obj = (DomainObject) domainObject;
		var pro = (DomainProperty) property;

		var propertyValue = obj.getValue(pro);
		Validate(obj, pro, propertyValue, result);
	}

	protected abstract void Validate(DomainObject domainObject, DomainProperty property, TValue propertyValue,
			ValidationResult result);
}

/// <summary>
/// 非泛型版
/// </summary>
public abstract class PropertyValidator:IPropertyValidator
{

	public void Validate(IDomainObject domainObject, IDomainProperty property, ValidationResult result) {
		var obj = (DomainObject) domainObject;
		var pro = (DomainProperty) property;
		var propertyValue = obj.GetValue(pro);
		Validate(obj, pro, propertyValue, result);
	}

	protected abstract void Validate(DomainObject domainObject, DomainProperty property, object propertyValue, ValidationResult result);
}

/// <summary>
/// 既能验证目标类型，也能验证当目标类型为集合的成员类型时的情况
/// </summary>
/// <typeparam name="TValue"></typeparam>
public abstract class PropertyListabelValidator<TValue> :IPropertyValidator
{

	public void Validate(IDomainObject domainObject, IDomainProperty property, ValidationResult result)
    {
        var obj = (DomainObject)domainObject;
        var pro = (DomainProperty)property;

        if (pro.PropertyType.IsList())
        {
            var values = obj.GetValue(pro) as IEnumerable;
            foreach (var value in values)
            {
                Validate(obj, pro, (TValue)value, result);
            }
        }
        else
        {
            var propertyValue = obj.GetValue<TValue>(pro);
            Validate(obj, pro, propertyValue, result);
        }
    }

	protected abstract void Validate(DomainObject domainObject, DomainProperty property, TValue propertyValue, ValidationResult result);
}