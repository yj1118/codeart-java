package apros.codeart.ddd.internal;

import static apros.codeart.runtime.Util.propagate;

import java.util.function.Function;

import apros.codeart.dto.IDTOSchema;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

import apros.codeart.ddd.DomainCollection;
import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.dynamic.IDynamicObject;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.ConstructorRepositoryImpl;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.PrimitiveUtil;

public final class DTOMapper {
    private DTOMapper() {
    }

    private static final Function<String, IDTOSchema> _getSchema = LazyIndexer.init((schemaCode) -> {
        if (StringUtil.isNullOrEmpty(schemaCode)) return DTObject.Empty;
        return DTObject.readonly(schemaCode);
    });

    public static DTObject toDTO(DomainObject target, String schemaCode, Function<DomainObject, Iterable<String>> getPropertes) {
        var schema = _getSchema.apply(schemaCode);
        return toDTO(target, schema, getPropertes, (p) -> p);
    }

    public static <T> DTObject toDTO(DomainObject target, String schemaCode, Function<DomainObject, Iterable<T>> getPropertyNames,
                                     Function<T, String> getPropertyName) {
        var schema = _getSchema.apply(schemaCode);
        return toDTO(target, schema, getPropertyNames, getPropertyName);
    }

    @SuppressWarnings("unchecked")
    public static <T> DTObject toDTO(DomainObject target, IDTOSchema schema, Function<DomainObject, Iterable<T>> getPropertyNames,
                                     Function<T, String> getPropertyName) {
        var properties = getPropertyNames.apply(target);

        var data = DTObject.Empty;
        for (var property : properties) {
            var propertyName = getPropertyName.apply(property);
            var memberName = schema.matchChildSchema(propertyName);

            if (!StringUtil.isNullOrEmpty(memberName)) {
                if (data.isEmpty()) data = DTObject.editable();
            } else
                continue;

            var value = target.getValue(propertyName);
            var obj = TypeUtil.as(value, DomainObject.class);
            if (obj != null) {
                var childSchema = schema.getChildSchema(propertyName);
                value = toDTO(obj, childSchema, getPropertyNames, getPropertyName); // 对象
                data.setValue(memberName, value);
                continue;
            }

            var list = TypeUtil.as(value, Iterable.class);
            if (list != null) {
                // 集合
                var childSchema = schema.getChildSchema(propertyName);
                data.push(propertyName, list, (item) -> {
                    var o = TypeUtil.as(item, DomainObject.class);
                    if (o != null)
                        return toDTO(o, childSchema, getPropertyNames, getPropertyName); // 对象

                    return DTObject.value(item);
                });
                continue;
            }

            data.setValue(memberName, value); // 值
        }
        return data;
    }

    /**
     * 从dto中加载数据
     */
    @SuppressWarnings("unchecked")
    public static void load(DomainObject target, DTObject data, boolean markChanged) {
        var meta = ObjectMetaLoader.get(target.getClass());

        for (var property : meta.properties()) {
            var value = data.getValue(property.name(), false);
            if (value == null)
                continue;

            var obj = TypeUtil.as(value, DTObject.class);
            if (obj != null) {
                target.loadValue(property.name(), getObjectValue(target, property, obj), markChanged);
                continue;
            }

            var objs = TypeUtil.as(value, Iterable.class);
            if (objs != null) {
                target.loadValue(property.name(), getListValue(target, property, objs), markChanged);
                continue;
            }
            target.loadValue(property.name(), getPrimitiveValue(target, property, value), markChanged);
        }
    }

    private static Object getObjectValue(DomainObject parent, PropertyMeta property, DTObject value) {
        switch (property.category()) {
            case DomainPropertyCategory.AggregateRoot:
            case DomainPropertyCategory.EntityObject:
            case DomainPropertyCategory.ValueObject: {

                var objType = property.monotype();
                return createInstance(objType, value);
            }
            default:
                throw new DomainDrivenException(Language.strings("DomainObjectLoadError", parent.getClass().getName()));
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object getListValue(DomainObject parent, PropertyMeta property, Iterable<DTObject> values) {
        var elementType = property.monotype();
        String propertyInParent = property.name();

        // 按照道理说，运行时是可以将DomainCollection转换为 DomainCollection<E>的，因为类型擦除了，但是还是测试下比较好
        var list = new DomainCollection(elementType, propertyInParent);
        list.setParent(parent);

        return switch (property.category()) {
            case DomainPropertyCategory.AggregateRootList, DomainPropertyCategory.EntityObjectList,
                 DomainPropertyCategory.ValueObjectList -> {
                for (DTObject value : values) {
                    var obj = createInstance(elementType, value);
                    list.add(obj);
                }
                yield list;
            }
            case DomainPropertyCategory.PrimitiveList -> {
                for (DTObject value : values) {
                    if (!value.isSingleValue())
                        throw new DomainDrivenException(
                                Language.strings("DomainObjectLoadError", parent.getClass().getName()));
                    list.add(value.getValue());
                }
                yield list;
            }
            default ->
                    throw new DomainDrivenException(Language.strings("DomainObjectLoadError", parent.getClass().getName()));
        };

    }

    private static Object getPrimitiveValue(DomainObject parent, PropertyMeta property, Object value) {
        if (property.category() == DomainPropertyCategory.Primitive) {
            return PrimitiveUtil.convert(value, property.monotype());
        }
        throw new DomainDrivenException(Language.strings("DomainObjectLoadError", parent.getClass().getName()));
    }

    static DomainObject createInstance(Class<?> objectType, DTObject data) {
        if (data.isEmpty())
            return DomainObject.getEmpty(objectType);

        var obj = constructObject(objectType, data);
        load(obj, data, false);
        return obj;
    }

    private static DomainObject constructObject(Class<?> objectType, DTObject data) {

        try {

            if (IDynamicObject.class.isAssignableFrom(objectType))
                return (DomainObject) Activator.createInstance(objectType);

            var constructorTip = ConstructorRepositoryImpl.getTip(objectType, false);
            if (constructorTip == null) {
                // 调用无参构造
                return (DomainObject) Activator.createInstance(objectType);
            }
            var constructor = constructorTip.constructor();
            var args = createArguments(constructorTip, data);
            return (DomainObject) constructor.newInstance(args);
        } catch (Exception ex) {
            throw propagate(ex);
        }
    }

    private static Object[] createArguments(ConstructorRepositoryImpl tip, DTObject data) {
        var length = Iterables.size(tip.parameters());

        if (length == 0)
            return ListUtil.emptyObjects();
        Object[] args = new Object[length];
        var prms = tip.parameters();
        var prmsLength = prms.size();
        for (var i = 0; i < prmsLength; i++) {
            var prm = prms.get(i);
            var arg = data.getValue(prm.name());
            args[i] = arg;
        }
        return args;
    }

}
