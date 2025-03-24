package apros.codeart.ddd.validation;

import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.metadata.ValueMeta;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class ValidatorUtil {

    private ValidatorUtil() {
    }

    /**
     * 常用于根据某一项数据的值，判断对象是否已经存在
     * 例如：在注册和修改账户对象时时，账户的的名称属性是不能重复的,
     * 该方法仅用于判断字符串类型的属性
     *
     * @param obj
     * @param propertyName
     * @param <T>
     * @return
     */
    private static <T extends IAggregateRoot> boolean isPropertyRepeated(T obj, String propertyName) {
        if (!obj.isPropertyDirty(propertyName)) return false;
        var propertyValue = obj.getPropertyValue(propertyName);

        String stringValue = TypeUtil.as(propertyValue, String.class);
        if (stringValue != null) {
            if (StringUtil.isNullOrEmpty(stringValue))
                return false;
        } else {
            if (ValueMeta.isDefaultValue(propertyValue)) return false;
        }

        var exp = _getPropertyNameCondition.apply(propertyName);

        var target = DataContext.using(() -> {
            return DataPortal.querySingle(obj.getClass(), exp, (data) ->
            {
                data.put(propertyName, propertyValue);
            }, QueryLevel.HOLD);
        });

        if (target.isEmpty()) return false;  //如果没有找到，那么没有重复
        if (target.equals(obj)) return false; //如果找到了但是跟obj一样，那么也不算重复
        return true;
    }

    private static final Function<String, String> _getPropertyNameCondition = LazyIndexer.init((propertyName) ->
    {
        return String.format("%s=@%s", propertyName, propertyName);
    });

//    /**
//     * 验证属性值是否重复
//     *
//     * @param obj
//     * @param propertyName
//     * @param result
//     * @param <T>
//     */
//    public static <T extends IAggregateRoot> void checkPropertyRepeated(@NotNull T obj, String propertyName, ValidationResult result) {
//        var property = DomainProperty.getProperty(obj.getClass(), propertyName);
//        checkPropertyRepeated(obj, propertyName, result);
//    }


    /**
     * 验证属性值是否重复
     *
     * @param obj
     * @param property
     * @param result
     * @param <T>
     */
    public static <T extends IAggregateRoot> void checkPropertyRepeated(T obj, DomainProperty property, ValidationResult result) {
        checkPropertyRepeated(obj, property.name(), result);
    }

    public static <T extends IAggregateRoot> void checkPropertyRepeated(T obj, String propertyName, ValidationResult result) {
        if (isPropertyRepeated(obj, propertyName)) {
            var property = obj.meta().findProperty(propertyName);
            var code = _getPropertyRepeatedErrorCode.apply(property);
            String value = obj.getPropertyValue(propertyName).toString();
            result.append(code, Language.strings("apros.codeart.ddd", "PropertyValueRepeated", property.call(), value));
        }
    }

    private static final Function<PropertyMeta, String> _getPropertyRepeatedErrorCode = LazyIndexer.init(((property) ->
    {
        return String.format("%s.%sRepeated", property.declaringType().getSimpleName(), property.name());
    }));

}
