package apros.codeart.ddd.repository;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Constructor;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;

public class ConstructorRepositoryImpl {
    private Constructor<?> _constructor;

    public Constructor<?> constructor() {
        return _constructor;
    }

    private ObjectMeta _objectTip;

    public ObjectMeta objectTip() {
        return _objectTip;
    }

    /// <summary>
    /// 构造该对象所用到的仓储接口的类型
    /// </summary>
    public Class<?> repositoryInterfaceType() {
        return this.objectTip().repositoryTip().repositoryInterfaceType();
    }

    public ConstructorRepositoryImpl(Constructor<?> constructor) {
        _constructor = constructor;
        initObject(constructor);
        initParameters(constructor);
    }

    private void initObject(Constructor<?> constructor) {
        var objectType = constructor.getDeclaringClass();
        _objectTip = ObjectMetaLoader.get(objectType);
    }

//	#region 参数信息

    private void initParameters(Constructor<?> constructor) {
        var originals = constructor.getParameters();
        ImmutableList.Builder<ConstructorParameterInfo> builder = ImmutableList.builder();
        for (var original : originals) {
            var prm = new ConstructorParameterInfo(this, original);
            builder.add(prm);
        }
        _parameters = builder.build();
    }

    ImmutableList<ConstructorParameterInfo> _parameters;

    public ImmutableList<ConstructorParameterInfo> parameters() {
        return _parameters;
    }

    public static ConstructorRepositoryImpl getTip(Class<?> objectType, boolean throwError) {
        var tip = _getTip.apply(objectType);
        if (tip == null && throwError) {
            throw new DomainDrivenException(
                    Language.strings("apros.codeart.ddd", "NoRepositoryConstructor", objectType.getName()));
        }
        return tip;
    }

    private static final Function<Class<?>, ConstructorRepositoryImpl> _getTip = LazyIndexer.init((objectType) -> {
        try {
            Constructor<?>[] constructors = objectType.getDeclaredConstructors();

            var target = ListUtil.find(constructors, (c) -> {
                return c.getAnnotation(ConstructorRepository.class) != null;
            });

            if (target == null) {
                return null;
            }

            return new ConstructorRepositoryImpl(target);
        } catch (Throwable ex) {
            throw propagate(ex);
        }
    });
}