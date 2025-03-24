package apros.codeart.ddd;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccessImpl;

/**
 * 属性验证器支持两种写法：
 * <p>
 * 1.注解式(Email/EmailValiator)
 * <p>
 * 2.验证器类定义(PropertyValidator(XXXSpection.class))
 */
public abstract class PropertyValidatorImpl implements IPropertyValidator {

    private final Annotation _tip;

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getTip(Class<T> annType) {
        return (T) _tip;
    }

    public PropertyValidatorImpl(Annotation tip) {
        _tip = tip;
    }

    @Override
    public void validate(IDomainObject domainObject, PropertyMeta property, ScheduledActionType actionType, ValidationResult result) {

        // 属性验证在删除对象时不必验证
        if (actionType == ScheduledActionType.Delete) return;

        var obj = (DomainObject) domainObject;
        var propertyValue = obj.getValue(property.name());
        validate(obj, property, propertyValue, actionType, result);
    }

    protected abstract void validate(DomainObject domainObject, PropertyMeta property, Object propertyValue,
                                     ScheduledActionType actionType, ValidationResult result);

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
            var annName = ann.annotationType().getName();

            var validatorType = TypeUtil.getClass(String.format("%sValidator", annName),
                    ann.annotationType().getClassLoader());
            if (validatorType == null)
                continue;

            SafeAccessImpl.checkUp(validatorType);

            // 将标记的注解作为构造函数的参数传入
            var validator = Activator.createInstance(validatorType, ann);
            validators.add((IPropertyValidator) validator);
        }
    }

    /**
     * 通过验证器获得对应得注解得类名
     *
     * @param validator
     * @return
     */
    public static String getAnnotationName(IPropertyValidator validator) {
        var simpleName = validator.getClass().getSimpleName();
        return simpleName.substring(0, simpleName.length() - "Validator".length());
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
        return SafeAccessImpl.createSingleton(validatorType);
    }

}