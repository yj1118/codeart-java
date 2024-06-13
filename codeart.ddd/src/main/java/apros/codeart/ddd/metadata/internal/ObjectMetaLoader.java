package apros.codeart.ddd.metadata.internal;

import static apros.codeart.i18n.Language.strings;
import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import apros.codeart.ddd.DerivedClassImpl;
import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.metadata.DomainObjectCategory;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.ObjectRepositoryTip;
import apros.codeart.ddd.repository.ObjectRepositoryImpl;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;

public final class ObjectMetaLoader {

    private ObjectMetaLoader() {
    }

    // 数据量不大，又不需要区分大小写，所以用 TreeMap<>(String.CASE_INSENSITIVE_ORDER)
    private static Map<String, ObjectMeta> _metas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private static ObjectMeta create(Class<?> domainType) {

        var types = TypeUtil.getInheriteds(domainType);

        // 先要初始化基类的
        for (var type : types) {
            if (!isMetadatable(type))
                continue;

            var typeName = type.getSimpleName();
            if (_metas.containsKey(typeName))
                continue;

            var meta = createByClass(type);
            _metas.put(typeName, meta);
        }

        {
            // 再创建自身
            var typeName = domainType.getSimpleName();
            var meta = createByClass(domainType);
            _metas.put(typeName, meta);
            return meta;
        }
    }

    private static ObjectMeta createByClass(Class<?> objectType) {
        var name = objectType.getSimpleName();
        var category = getCategory(objectType);
        var validators = ObjectValidatorUtil.getValidators(objectType);

        ObjectRepositoryTip repositoryTip = null;
        var repository = ObjectRepositoryImpl.getTip(objectType, false);
        if (repository != null) {
            repositoryTip = new ObjectRepositoryTip(repository.repositoryInterfaceType());
        }

        return new ObjectMeta(name, objectType, category, validators, repositoryTip);
    }

    /**
     * @param objectName
     * @return
     */
    public static ObjectMeta get(String objectName) {
        // 由于是初始化期间执行的obtain，初始化完毕后，才会执行get，所以不会有并发问题,因此没有加锁
        // 初始化期间是单线程的，尚未接受外界请求，所以也是安全的
        var obj = _metas.get(objectName);
        if (obj == null) {
            throw new DomainDrivenException(Language.strings("apros.codeart.ddd", "NotFoundDomainObject", objectName));
        }
        return obj;
    }

    public static boolean exists(String objectName) {
        var obj = _metas.get(objectName);
        return obj != null;
    }

    public static ObjectMeta get(Class<?> objectType) {
        return get(objectType.getSimpleName());
    }

    public static ObjectMeta tryGet(Class<?> objectType) {
        if (ObjectMeta.isDomainObject(objectType))
            return get(objectType);

        return null; // 非领域类型是没有元数据的
    }

    static void load(Iterable<Class<? extends IDomainObject>> domainTypes) {

        // 为了防止循环引用导致的死循环，要先预加载（只加载类型，不加载属性信息）
        for (var domainType : domainTypes) {
            create(domainType);
        }

        // 再加载完整定义，即出发静态构造
        for (var domainType : domainTypes) {
            staticConstructor(domainType);

            // 尝试为派生类做支持
            DerivedClassImpl.init(domainType);
        }

        // 全部执行完毕后，再触发ObjectMeta的合并方法，使继承链的信息合并到子类
        for (var domainType : domainTypes) {
            var meta = get(domainType);
            meta.merge();
        }

        // 最后移除多余的元数据（是由基类建立的过度元数据）
        var typeNames = _metas.keySet().toArray();
        for (var typeName : typeNames) {
            if (!ListUtil.contains(domainTypes, (t) -> t.getSimpleName().equalsIgnoreCase((String) typeName))) {
                _metas.remove(typeName);
            }
        }
    }

    /**
     * 获得类型的第一个静态字段
     *
     * @param type
     * @return
     */
    private static Field firstStaticField(Class<?> type) {
        Field[] fields = type.getDeclaredFields(); // 注意，这个方法只会获得类自身的字段定义，不会包含父级

        for (Field field : fields) {
            if (FieldUtil.isStatic(field))
                return field;
        }
        return null;
    }

    /**
     * 通过调用一个静态成员，来模拟触发领域对象类型的静态构造函数
     * <p>
     * 如果类型没有静态成员，那么就算不触发静态构造也不会影响，因为不需要注入领域属性和空对象
     *
     * @param objectType
     */
    private static void staticConstructor(Class<?> objectType) {
        try {
            // 我们需要从基类开始，依次调用
            var types = TypeUtil.getInheriteds(objectType);

            for (var type : types) {
                if (!isMetadatable(type))
                    continue;

                var field = firstStaticField(type);
                if (field != null) {
                    field.setAccessible(true);
                    field.get(null);// 获取一次静态值，触发静态构造
                }

            }

            // 再触发自身
            {
                var field = firstStaticField(objectType);
                if (field != null) {
                    field.setAccessible(true);
                    field.get(null);
                }
            }

        } catch (Exception e) {
            throw propagate(e);
        }

    }

    public static boolean isMetadatable(Class<?> objectType) {
        if (ObjectMeta.isAggregateRoot(objectType) || ObjectMeta.isEntityObject(objectType)
                || ObjectMeta.isValueObject(objectType))
            return true;

        return false;

    }

    private static DomainObjectCategory getCategory(Class<?> objectType) {
        if (ObjectMeta.isAggregateRoot(objectType))
            return DomainObjectCategory.AggregateRoot;

        if (ObjectMeta.isEntityObject(objectType))
            return DomainObjectCategory.EntityObject;

        if (ObjectMeta.isValueObject(objectType))
            return DomainObjectCategory.ValueObject;

        throw new IllegalArgumentException(strings("apros.codeart.ddd", "TypeMismatch"));
    }
}
