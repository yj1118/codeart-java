package apros.codeart.ddd;

import java.lang.annotation.*;

/**
 * 从注解中修正属性元数据信息，这主要用于子类修正父类定义的领域属性
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ObjectProperties.class)
public @interface ObjectProperty {

    /**
     * 领域属性的名称
     *
     * @return
     */
    String name();

    /**
     * 领域属性的类型（如果是集合，那么就是成员类型，如果不是集合，那么就是属性类型）
     *
     * @return
     */
    Class<?> type();
}
