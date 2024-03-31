package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.apros.codeart.bytecode.ClassGenerator;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.ListUtil;

public final class Activator {
	private Activator() {
	}

	public static Object createInstance(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			return createInstance(clazz);
		} catch (Exception e) {
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
			return method.invoke(null, instanceType);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static Function<Class<?>, Method> getCreateInstanceMethod = LazyIndexer.init((objectType) -> {
		return generateCreateInstanceMethod(objectType);
	});

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

			return cls.getDeclaredMethod(methodName, objectType);

		} catch (Exception e) {
			throw propagate(e);
		}
	}
	
	
	/**
	 * 
	 * 找到实现了某个接口或继承了哪个类的所有类的类型
	 * 
	 * @param <T>
	 * @param superType
	 * @param archives
	 * @return
	 */
	public static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> superType, String...archives){
		
		var urls = ListUtil.mapMany(archives, (archive)->{
			return ClasspathHelper.forPackage(archive);
		});
		
		// 创建一个Reflections实例，指定要扫描的包
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(urls)
                .setScanners(Scanners.SubTypes));

        return reflections.getSubTypesOf(superType);
	}
	
	public static <T> Set<Class<?>> getAnnotatedTypesOf(Class<? extends Annotation> annotation, String...archives){
		
		var urls = ListUtil.mapMany(archives, (archive)->{
			return ClasspathHelper.forPackage(archive);
		});
		
		// 创建一个Reflections实例，指定要扫描的包
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(urls)
                .setScanners(Scanners.TypesAnnotated));

        return reflections.getTypesAnnotatedWith(annotation);
	}
	
	

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
//        DynamicMethod method = new DynamicMethod(string.Format("CreateInstanceByConstructor_{0}", Guid.NewGuid().ToString("n"))
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
