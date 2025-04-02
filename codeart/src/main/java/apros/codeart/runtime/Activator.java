package apros.codeart.runtime;

import static apros.codeart.runtime.Util.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import apros.codeart.bytecode.ClassGenerator;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;

public final class Activator {
    private Activator() {
    }

    public static Object createInstance(String className, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            return createInstance(clazz, args);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    public static Object createInstance(Class<?> clazz, Object... args) {
        var types = new Class<?>[1 + args.length];
        types[0] = clazz;
        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            if (arg instanceof Annotation) {
                types[i + 1] = ((Annotation) arg).annotationType();
            } else {
                types[i + 1] = arg.getClass();
            }
        }
        return createInstance(types, args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<T> exceptType, String className, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            return (T) createInstance(clazz, args);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    /**
     * 创建实例，IL的实现，高效率
     *
     * @return
     */
    public static Object createInstance(Class<?> instanceType) {
        try {
            var method = getCreateInstanceMethod.apply(instanceType);
            return method.invoke(null);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    /**
     * 创建实例，IL的实现，高效率
     *
     * @return
     */
    public static Object createInstance(Class<?>[] instanceandArgTypes, Object... args) {
        try {
            // 注意，数组是不能作为hashMap的key得，因为数组得hashCode是该数组得内存地址，而不是成员内容
            ArrayList<Class<?>> key = ListUtil.asList(instanceandArgTypes);
            var method = getCreateInstanceMethodByListTypes.apply(key);
            return method.invoke(null, args);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    private static Function<Class<?>, Method> getCreateInstanceMethod = LazyIndexer.init((objectType) -> {
        return generateCreateInstanceMethod(objectType);
    });

    private static Function<ArrayList<Class<?>>, Method> getCreateInstanceMethodByListTypes = LazyIndexer
            .init((types) -> {
                return generateCreateInstanceMethod(types);
            });

    /**
     * 专门为无参构造单独写个方法，提高这类需求的执行效率
     *
     * @param objectType
     * @return
     */
    private static Method generateCreateInstanceMethod(Class<?> objectType) {

        String methodName = String.format("createInstance_%s", objectType.getSimpleName());

        try (var cg = ClassGenerator.define()) {

            try (var mg = cg.defineMethodPublicStatic(methodName, objectType)) {
                var obj = mg.declare(objectType, "obj");
                mg.assign(obj, () -> {
                    mg.newObject(objectType);
                });

                obj.load();
            }

            // 返回生成的字节码
            var cls = cg.toClass();

            return cls.getDeclaredMethod(methodName);

        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    private static Method generateCreateInstanceMethod(ArrayList<Class<?>> types) {
        var objectType = types.get(0);
        Class<?>[] argTypes = new Class<?>[types.size() - 1];
        for (var i = 0; i < argTypes.length; i++) {
            argTypes[i] = types.get(i + 1);
        }

        String methodName = String.format("createInstance_%s", objectType.getSimpleName());

        try (var cg = ClassGenerator.define()) {

            try (var mg = cg.defineMethodPublicStatic(methodName, objectType, (args) -> {
                for (var i = 0; i < argTypes.length; i++) {
                    args.add(String.format("arg%s", i), argTypes[i]);
                }

            })) {
                var obj = mg.declare(objectType, "obj");
                mg.assign(obj, () -> {

                    mg.newObject(objectType, () -> {
                        for (var i = 0; i < argTypes.length; i++) {
                            mg.loadVariable(String.format("arg%s", i));
                        }
                    });
                });

                obj.load();
            }

            // 返回生成的字节码
            var cls = cg.toClass(Activator.class.getClassLoader());

            return cls.getDeclaredMethod(methodName, argTypes);

        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    /**
     * 找到实现了某个接口或继承了哪个类的所有类的类型
     *
     * @param <T>
     * @param superType
     * @param archives
     * @return
     */
    public static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> superType, String... archives) {

        var urls = ListUtil.mapMany(archives, (archive) -> {
            return ClasspathHelper.forPackage(archive);
        });

        // 创建一个Reflections实例，指定要扫描的包
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(urls).setScanners(Scanners.SubTypes));

        return reflections.getSubTypesOf(superType);
    }

    public static <T> Set<Class<?>> getAnnotatedTypesOf(Class<? extends Annotation> annotation, String... archives) {

        var urls = ListUtil.mapMany(archives, (archive) -> {
            return ClasspathHelper.forPackage(archive);
        });

        // 创建一个Reflections实例，指定要扫描的包
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(urls).setScanners(Scanners.TypesAnnotated));

        return reflections.getTypesAnnotatedWith(annotation);
    }

//	public static Class<?> getClass(String pattern) {
//
//		var archives = App.archives();
//
//		var urls = ListUtil.mapMany(archives, (archive) -> {
//			return ClasspathHelper.forPackage(archive);
//		});
//
//		// 创建一个Reflections实例，指定要扫描的包
//		Reflections reflections = new Reflections(
//				new ConfigurationBuilder().setUrls(urls).setScanners(Scanners.TypesAnnotated, Scanners.SubTypes));
//
//		// 查找符合条件的类
//		Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
//
//		// 过滤类名以匹配模糊查找
//		var target = classes.stream().filter(clazz -> clazz.getSimpleName().contains(pattern)).findFirst();
//		if (target.isEmpty())
//			return null;
//		return target.get();
//	}

//	/// <summary>
//	/// 创建实例，IL的实现，高效率
//	/// </summary>
//	/// <param name="instanceType"></param>
//	/// <returns></returns>
//	public static Object createInstance(ConstructorInfo constructor, Object[] args)
//    {
//        CreateInstanceMethod method = null;
//        if (!_getCreateInstanceByConstructorMethods.TryGetValue(constructor, out method))
//        {
//            if (method == null)
//            {
//                lock (_getCreateInstanceByConstructorMethods)
//                {
//                    if (!_getCreateInstanceByConstructorMethods.TryGetValue(constructor, out method))
//                    {
//                        if (method == null)
//                        {
//                            method = GenerateCreateInstanceMethod(constructor);
//                            _getCreateInstanceByConstructorMethods.Add(constructor, method);
//                        }
//                    }
//                }
//            }
//        }
//
//        var invoke = (Func<object[], object>)method.Invoke;
//        return invoke(args);
//    }
//
//	private static Dictionary<ConstructorInfo, CreateInstanceMethod> _getCreateInstanceByConstructorMethods = new Dictionary<ConstructorInfo, CreateInstanceMethod>();
//
//	private static CreateInstanceMethod GenerateCreateInstanceMethod(ConstructorInfo constructor)
//    {
//        var objectType = constructor.DeclaringType;
//        DynamicMethod method = new DynamicMethod(string.Format("CreateInstanceByConstructor_{0}", GUID.NewGuid().ToString("n"))
//                                                , typeof(object)
//                                                , new Type[] { typeof(object[]) }
//                                                , true);
//
//        MethodGenerator g = new MethodGenerator(method);
//        //以下代码把数组参数转成，new T(arg0,arg1)的形式
//        var result = g.Declare(objectType,"result");
//        var objs = g.Declare<object[]>();
//        g.Assign(objs, () =>
//        {
//            g.LoadParameter(0);
//        });
//
//        g.Assign(result, () =>
//        {
//            g.NewObject(constructor, () =>
//             {
//                 var index = g.Declare<int>();
//                 var prms = constructor.GetParameters();
//                 for (var i = 0; i < prms.Length; i++)
//                 {
//                     g.Assign(index, () =>
//                     {
//                         g.Load(i);
//                     });
//
//                     g.LoadElement(objs, index);
//                     g.Cast(prms[i].ParameterType);
//                 }
//             });
//        });
//
//        g.LoadVariable("result");
//        g.Cast(typeof(object));
//        g.Return();
//
//        var invoke = method.CreateDelegate(typeof(Func<object[], object>));
//        return new CreateInstanceMethod(invoke);
//    }
//
//	#endregion

}
