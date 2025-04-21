package apros.codeart.runtime;

import static apros.codeart.i18n.Language.strings;
import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;
import java.util.function.Function;

import apros.codeart.Memoized;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

/**
 *
 */
public final class MethodUtil {
    private MethodUtil() {
    }

    public static Method get(String fullMethodName) {
        try {
            var lastDot = fullMethodName.lastIndexOf(".");
            var className = StringUtil.substr(fullMethodName, 0, lastDot);
            var methodName = StringUtil.substr(fullMethodName, lastDot + 1);
            var cls = Class.forName(className);
            return cls.getMethod(methodName);
        } catch (Throwable ex) {
            throw propagate(ex);
        }
    }

    private static final Function<Class<?>, Function<String, Iterable<Method>>> _getMethods = LazyIndexer.init((objCls) -> {
        return LazyIndexer.init((methodName) -> {
            return findMethods(objCls, methodName);
        });
    });

    /**
     * 不区分方法大小写，我们实际写程序也不可能写两个名字大小写不同，但是字母相同的方法
     *
     * @param objCls
     * @param methodName
     * @return
     */
    private static Iterable<Method> findMethods(Class<?> objCls, String methodName) {
        Method[] methods = objCls.getMethods();

        return ListUtil.filter(methods, (m) -> {
            return m.getName().equalsIgnoreCase(methodName);
        });
    }

    /**
     * resolve得到的方法，如果没有，则返回null,不会报错
     *
     * @param objCls
     * @param methodName
     * @param parameterTypes
     * @return
     */
    @Memoized
    public static Method resolve(Class<?> objCls, String methodName, Class<?>[] parameterTypes, Class<?> returnType) {

        var methods = _getMethods.apply(objCls).apply(methodName);

        if (parameterTypes == null) {
            return ListUtil.find(methods, (m) -> {
                return m.getParameterCount() == 0 && m.getReturnType().equals(returnType);
            });
        }

        for (var method : methods) {
            if (method.getParameterCount() != parameterTypes.length) {
                continue;
            }
            var argTypes = method.getParameterTypes();
            boolean finded = true;
            for (var i = 0; i < argTypes.length; i++) {
                if (!argTypes[i].equals(parameterTypes[i])) {
                    finded = false;
                    break;
                }
            }

            if (!method.getReturnType().equals(returnType))
                finded = false;
            if (finded)
                return method;
        }
        return null;
    }

    @Memoized
    public static Method resolve(Class<?> objCls, String methodName) {
        return resolve(objCls, methodName, null);
    }

    /**
     * 忽略返回值得到方法
     *
     * @param objCls
     * @param methodName
     * @param parameterTypes
     * @return
     */
    @Memoized
    public static Method resolve(Class<?> objCls, String methodName, Class<?>[] parameterTypes) {

        return resolve(objCls, methodName, parameterTypes, (argType, targetType) -> {
            return argType.equals(targetType);
        });

    }

    /**
     * 可以接收参数重载的方式查找方法
     *
     * @param objCls
     * @param methodName
     * @param parameterTypes
     * @return
     */
    @Memoized
    public static Method resolveLike(Class<?> objCls, String methodName, Class<?>[] parameterTypes) {

        // 优先找出强类型匹配的
        var method = resolve(objCls, methodName, parameterTypes, (argType, targetType) -> {
            return argType == targetType;
        });

        if (method == null) {
            method = resolve(objCls, methodName, parameterTypes, (argType, targetType) -> {
                return argType.isAssignableFrom(targetType);
            });
        }

        return method;

    }

    @Memoized
    private static Method resolve(Class<?> objCls, String methodName, Class<?>[] parameterTypes,
                                  BiFunction<Class<?>, Class<?>, Boolean> compare) {

        var methods = _getMethods.apply(objCls).apply(methodName);

        if (parameterTypes == null) {
            return ListUtil.find(methods, (m) -> {
                return m.getParameterCount() == 0;
            });
        }

        for (var method : methods) {
            if (method.getParameterCount() != parameterTypes.length) {
                continue;
            }
            var argTypes = method.getParameterTypes();
            boolean finded = true;
            for (var i = 0; i < argTypes.length; i++) {
                if (!compare.apply(argTypes[i], parameterTypes[i])) {
                    finded = false;
                    break;
                }
            }
            if (finded)
                return method;
        }
        return null;
    }

    @Memoized
    public static Method resolveByName(Class<?> objCls, String methodName) {

        var methods = _getMethods.apply(objCls).apply(methodName);
        return ListUtil.first(methods);
    }

    /**
     * 临时使用一次方法，不做缓存处理
     *
     * @param objCls
     * @param methodName
     * @return
     */
    public static Method resolveByNameOnce(Class<?> objCls, String methodName) {
        var methods = findMethods(objCls, methodName);
        return ListUtil.first(methods);
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    /**
     * 执行静态方法
     *
     * @param objClas
     * @param staticMethodName
     */
    public static Object invoke(Class<?> objClas, String staticMethodName) {
        try {

            Method staticMethod = objClas.getDeclaredMethod(staticMethodName);

            // 调用静态方法（因为是静态方法，所以传入 null 作为实例参数）
            return staticMethod.invoke(null);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }


    public static Object invoke(Class<?> objClas, String methodName, Object self, Object... args) {
        try {

            Method method = objClas.getDeclaredMethod(methodName);
            return method.invoke(self, args);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

}
