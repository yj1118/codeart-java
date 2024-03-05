package com.apros.codeart.runtime;

import java.util.HashMap;

public final class ObjectUtil {
	private ObjectUtil() {
	}

	/**
	 * 创建实例，IL的实现，高效率
	 * 
	 * @return
	 */
	public static Object createInstance(Class<?> instanceType) {
		CreateInstanceMethod method = _getCreateInstanceMethods.getOrDefault(instanceType, null);
		if (method == null) {
			synchronized (_getCreateInstanceMethods) {
				method = _getCreateInstanceMethods.getOrDefault(instanceType, null);
				if (method == null) {
					method = generateCreateInstanceMethod(instanceType);
					_getCreateInstanceMethods.put(instanceType, method);
				}
			}
		}

		var invoke = (Func<object>) method.Invoke;
		return invoke();
	}

	private static HashMap<Class<?>, CreateInstanceMethod> _getCreateInstanceMethods = new HashMap<Class<?>, CreateInstanceMethod>();

	private static CreateInstanceMethod generateCreateInstanceMethod(Class<?> objectType)
    {
        DynamicMethod method = new DynamicMethod(string.Format("CreateInstance_{0}", Guid.NewGuid().ToString("n"))
                                                , typeof(object)
                                                , Array.Empty<Type>()
                                                , true);

        MethodGenerator g = new MethodGenerator(method);

        var result = g.Declare<object>("result");
        g.Assign(result, () =>
        {
            g.NewObject(objectType);
            g.Cast(typeof(object));
        });

        g.LoadVariable("result");
        g.Return();

        var invoke = method.CreateDelegate(typeof(Func<object>));
        return new CreateInstanceMethod(invoke);
    }

	private class CreateInstanceMethod {
		public object Invoke
		{
            get;
            private set;
        }

		public CreateInstanceMethod(object invoke) {
			this.Invoke = invoke;
		}
	}

	/// <summary>
	/// 创建实例，IL的实现，高效率
	/// </summary>
	/// <param name="instanceType"></param>
	/// <returns></returns>
	public static object CreateInstance(this ConstructorInfo constructor, object[] args)
    {
        CreateInstanceMethod method = null;
        if (!_getCreateInstanceByConstructorMethods.TryGetValue(constructor, out method))
        {
            if (method == null)
            {
                lock (_getCreateInstanceByConstructorMethods)
                {
                    if (!_getCreateInstanceByConstructorMethods.TryGetValue(constructor, out method))
                    {
                        if (method == null)
                        {
                            method = GenerateCreateInstanceMethod(constructor);
                            _getCreateInstanceByConstructorMethods.Add(constructor, method);
                        }
                    }
                }
            }
        }

        var invoke = (Func<object[], object>)method.Invoke;
        return invoke(args);
    }

	private static Dictionary<ConstructorInfo, CreateInstanceMethod> _getCreateInstanceByConstructorMethods = new Dictionary<ConstructorInfo, CreateInstanceMethod>();

	private static CreateInstanceMethod GenerateCreateInstanceMethod(ConstructorInfo constructor)
    {
        var objectType = constructor.DeclaringType;
        DynamicMethod method = new DynamicMethod(string.Format("CreateInstanceByConstructor_{0}", Guid.NewGuid().ToString("n"))
                                                , typeof(object)
                                                , new Type[] { typeof(object[]) }
                                                , true);

        MethodGenerator g = new MethodGenerator(method);
        //以下代码把数组参数转成，new T(arg0,arg1)的形式
        var result = g.Declare(objectType,"result");
        var objs = g.Declare<object[]>();
        g.Assign(objs, () =>
        {
            g.LoadParameter(0);
        });

        g.Assign(result, () =>
        {
            g.NewObject(constructor, () =>
             {
                 var index = g.Declare<int>();
                 var prms = constructor.GetParameters();
                 for (var i = 0; i < prms.Length; i++)
                 {
                     g.Assign(index, () =>
                     {
                         g.Load(i);
                     });

                     g.LoadElement(objs, index);
                     g.Cast(prms[i].ParameterType);
                 }
             });
        });

        g.LoadVariable("result");
        g.Cast(typeof(object));
        g.Return();

        var invoke = method.CreateDelegate(typeof(Func<object[], object>));
        return new CreateInstanceMethod(invoke);
    }

	#endregion

}
