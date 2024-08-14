package apros.codeart.dto.serialization.internal;

import org.apache.logging.log4j.util.Strings;

import apros.codeart.dto.DTObject;
import apros.codeart.util.Common;
import apros.codeart.util.StringUtil;

/**
 * 虽然 {@code DTObjectMapper}
 * 是public，但是apros.codeart.dto.serialization.internal包不导出，所以不会被外界访问
 */
public class DTObjectMapper {
    private DTObjectMapper() {
    }

    /**
     * 根据架构代码，将dto的数据创建到新实例<paramref name="instanceType"/>中
     *
     * @param instanceType
     * @param schemaCode
     * @param dto
     * @return
     */
    public static Object save(Class<?> instanceClass, String schemaCode, DTObject dto) {
        if (instanceClass == DTObject.class)
            return dto.clone();
        TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceClass, schemaCode);
        return typeInfo.deserialize(dto);
    }

    public static Object save(Class<?> instanceClass, DTObject dto) {
        return save(instanceClass, Strings.EMPTY, dto);
    }

    /// <summary>
    /// 根据架构代码，将dto的数据写入到新实例<paramref name="instanceType"/>中
    /// </summary>
    /// <param name="instance"></param>
    /// <param name="schemaCode"></param>
    /// <param name="dto"></param>
    public static void save(Object instance, String schemaCode, DTObject dto) {
        var instanceType = instance.getClass();
        if (instanceType.equals(DTObject.class))
            instance = dto.clone();
        TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceType, schemaCode);
        typeInfo.deserialize(instance, dto);
    }

    public static void save(Object instance, DTObject dto) {
        save(instance, StringUtil.empty(), dto);
    }

    /**
     * 据架构代码将对象的信息创建dto，默认返回的是可以编辑的对象
     *
     * @param schemaCode
     * @param instance
     * @return
     */
    public static DTObject load(String schemaCode, Object instance) {
        if (Common.isNull(instance))
            return DTObject.Empty;
        var instanceType = instance.getClass();
        if (instanceType.equals(DTObject.class))
            return (DTObject) instance;
        TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceType, schemaCode);
        return typeInfo.serialize(schemaCode, instance);
    }

    public static DTObject load(Object instance) {
        return load(StringUtil.empty(), instance);
    }

    /**
     * 根据架构代码将对象的信息加载到dto中
     *
     * @param dto
     * @param schemaCode
     * @param instance
     */
    public static void load(DTObject dto, String schemaCode, Object instance) {
        if (Common.isNull(instance))
            return;
        var instanceType = instance.getClass();
        if (instanceType.equals(DTObject.class))
            return;
        TypeSchemaCodeInfo typeInfo = TypeSchemaCodeInfo.getTypeInfo(instanceType, schemaCode);
        typeInfo.serialize(instance, dto);
    }
}