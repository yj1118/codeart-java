package apros.codeart.dto.serialization.internal;

import static apros.codeart.i18n.Language.strings;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Consumer;

import apros.codeart.bytecode.IVariable;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.dto.IDTOReader;
import apros.codeart.dto.IDTOWriter;
import apros.codeart.runtime.MethodUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ReaderWriterLockSlim;
import apros.codeart.util.StringUtil;

/**
 * 封装了根据类型获取序列化、反序列化的方法信息
 */
final class SerializationMethodHelper {

	private SerializationMethodHelper() {
	}

	private static final HashMap<Class<?>, Method> _serializeMethods = new HashMap<Class<?>, Method>();

	private static final HashMap<Class<?>, Method> _deserializeMethods = new HashMap<Class<?>, Method>();

	private static HashMap<Class<?>, Method> getMethods(SerializationMethodType methodType) {

		switch (methodType) {
		case SerializationMethodType.Serialize:
			return _serializeMethods;
		case SerializationMethodType.Deserialize:
			return _deserializeMethods;
		default:
			break;
		}

		return _deserializeMethods;
	}

	private static final ReaderWriterLockSlim _typeMethodsLock = new ReaderWriterLockSlim();

//
//	#region 重构
//
	private static Method getMethodByCache(Class<?> type, SerializationMethodType methodType) {

		Method method = _typeMethodsLock.readGet(() -> {
			var methodTypeMethods = getMethods(methodType);

			if (methodTypeMethods != null) {
				return methodTypeMethods.get(type);
			}
			return null;
		});

		return method;
	}

	private static Method getMethodBySimple(Class<?> type, SerializationMethodType methodType) {
		Method method = null;
		switch (methodType) {
		case SerializationMethodType.Serialize: {
			if (type.isEnum()) {
				method = MethodUtil.resolve(IDTOWriter.class, "writeEnum", new Class<?>[] { String.class, Enum.class });
			} else {
				String methodName = String.format("write%s", StringUtil.firstToUpper(type.getSimpleName()));
				method = MethodUtil.resolve(IDTOWriter.class, methodName, new Class<?>[] { String.class, type });
				if ((method == null) && (!type.isPrimitive())) {
					// 如果不是int、long等基础类型，而有可能是自定义类型，那么用以下代码得到方法
					method = MethodUtil.resolve(IDTOWriter.class, "writeObject", _writeObjectArgs);
				}
			}

			break;
		}
		case SerializationMethodType.Deserialize: {
			if (type.isEnum()) {
				method = MethodUtil.resolve(IDTOReader.class, "readEnum", _stringArgs);
			} else {
				String methodName = String.format("read%s", StringUtil.firstToUpper(type.getSimpleName()));
				method = MethodUtil.resolve(IDTOReader.class, methodName, _stringArgs);
				if ((method == null) && (!type.isPrimitive())) {
					// 如果不是int、long等基础类型，而有可能是自定义类型，那么用以下代码得到方法
					// IDTOWriter.ReadObject<T>(string name);
					method = MethodUtil.resolve(IDTOReader.class, "readObject",
							new Class<?>[] { Class.class, String.class });
				}
			}

			break;
		}
		default:
			break;
		}

		if (method == null) {
			throw new IllegalArgumentException(
					strings("codeart", "NotFoundDTOSerializationMethod", TypeUtil.resolveName(type)));
		}
		return method;

	}

	private static final Class<?>[] _writeObjectArgs = new Class<?>[] { String.class, Object.class };
	private static final Class<?>[] _stringArgs = new Class<?>[] { String.class };

//	#endregion

	private static final Method existMethod = MethodUtil.resolve(IDTOReader.class, "exist", _stringArgs);

	/// <summary>
	/// 根据类型得到序列化方法
	/// </summary>
	/// <param name="type"></param>
	/// <param name="methodType"></param>
	/// <returns></returns>
	public static Method getTypeMethod(Class<?> type, SerializationMethodType methodType) {
		if (methodType == SerializationMethodType.Exist)
			return existMethod;
		Method method = getMethodByCache(type, methodType);
		if (method == null) {
			// 对于枚举类型：根据基础类型找到适当的序列化器方法
			// 对于简单类型：找到对应的 IPrimitiveReader/IPrimitveWriter 方法
			// 对于复杂类型：根据类型得到适当的序列化器方法
			method = getMethodBySimple(type, methodType);

			// 更新方法缓存
			_typeMethodsLock.writeRun((m) -> {
				getMethods(methodType).put(type, m);
			}, method);

		}

		return method;
	}

	/**
	 * 获取方法的拥有者在参数中的序号
	 * 
	 * @param method
	 * @param methodType
	 * @return
	 */
	public static int getParameterIndex(Method method, SerializationMethodType methodType) {
		String typeName = method.getDeclaringClass().getName();
		if (methodType == SerializationMethodType.Serialize) {
//			if (typeName == DTObjectSerializer.class.getName())
//				return SerializationArgs.SerializerIndex;
			if (typeName == IDTOWriter.class.getName())
				return SerializationArgs.WriterIndex;
		} else {
//			if (typeName == DTObjectDeserializer.class.getName())
//				return SerializationArgs.DeserializerIndex;
			if (typeName == IDTOReader.class.getName())
				return SerializationArgs.ReaderIndex;
		}
		throw new IllegalArgumentException(strings("codeart", "NotFoundParameterIndex", typeName));
	}

	/// <summary>
	/// <para>得到写入某个类型的IL代码</para>
	/// <para>writer.Write(value); 或 serialzer.Serialze(value);</para>
	/// </summary>
	/// <param name="g"></param>
	/// <param name="valueType"></param>
	/// <param name="loadValue"></param>
	public static void write(MethodGenerator g, String dtoMemberName, Class<?> valueType,
			Consumer<Class<?>> loadValue) {
		var method = SerializationMethodHelper.getTypeMethod(valueType, SerializationMethodType.Serialize);
		var prmIndex = SerializationMethodHelper.getParameterIndex(method, SerializationMethodType.Serialize);
		g.invoke(prmIndex, method.getName(), () -> {
			g.load(dtoMemberName);
			var argType = method.getParameterTypes()[1];
			loadValue.accept(argType);

			// if (prmIndex == SerializationArgs.SerializerIndex)
			// {
			// 是serializer.Serializ();
			// g.LoadVariable(SerializationArgs.TypeNameTable);
			// }
		});
	}

	public static void writeBlob(MethodGenerator g, String dtoMemberName, Runnable loadValue) {
		var prmIndex = SerializationArgs.WriterIndex;
		g.invoke(prmIndex, "writeBlob", () -> {
			g.load(dtoMemberName);
			loadValue.run();
		});
	}

	public static void writeElement(MethodGenerator g, String dtoMemberName, Runnable loadValue) {

//      var method =  typeof(IDTOWriter).ResolveMethod("writeElement",
//                                                      new Type[] { elementType },
//                                                      MethodParameter.Create<string>(), 
//                                                      MethodParameter.Create<bool>(),
//                                                      MethodParameter.CreateGeneric(elementType));
		var prmIndex = SerializationArgs.WriterIndex;

		g.invoke(prmIndex, "writeElement", () -> {
			g.load(dtoMemberName);
			loadValue.run();
		});
	}

	public static void writeArray(MethodGenerator g, String dtoMemberName) {
		var method = MethodUtil.resolve(IDTOWriter.class, "writeArray", new Class<?>[] { String.class });
		var prmIndex = SerializationArgs.WriterIndex;

		g.invoke(prmIndex, method, () -> {
			g.load(dtoMemberName);
		});

//		g.invoke(method.getName(), () -> {
//			g.LoadParameter(prmIndex);
//			g.Load(dtoMemberName);
//		});
	}

//
	/// <summary>
	/// <para>得到读取某个类型的IL代码</para>
	/// <para>reader.ReadXXX(); 或 deserialzer.Deserialze();</para>
	/// </summary>
	/// <param name="g"></param>
	/// <param name="valueType"></param>
	/// <param name="loadValue"></param>
	public static void read(MethodGenerator g, String dtoMemberName, Class<?> valueType) {
		var method = SerializationMethodHelper.getTypeMethod(valueType, SerializationMethodType.Deserialize);
		var prmIndex = SerializationMethodHelper.getParameterIndex(method, SerializationMethodType.Deserialize);
		boolean isReadObject = method.getName().equals("readObject");

		g.invoke(prmIndex, method.getName(), () -> {

			if (isReadObject) {
				g.load(valueType);
			}

			g.load(dtoMemberName);
		});

		if (isReadObject) {
			// 类型转换
			g.cast(valueType);
		}

	}

	public static void exist(MethodGenerator g, String dtoMemberName, Class<?> valueType) {
		var method = SerializationMethodHelper.getTypeMethod(valueType, SerializationMethodType.Exist);
		var prmIndex = SerializationMethodHelper.getParameterIndex(method, SerializationMethodType.Deserialize);
		g.invoke(prmIndex, method.getName(), () -> {
			g.load(dtoMemberName);
		});
	}

	public static void readBlob(MethodGenerator g, String dtoMemberName) {
		var prmIndex = SerializationArgs.ReaderIndex;

		g.invoke(prmIndex, "readBlob", () -> {
			g.load(dtoMemberName);
		});
	}

	/// <summary>
	/// 读取数组的长度
	/// </summary>
	/// <param name="g"></param>
	/// <param name="dtoMemberName"></param>
	/// <param name="valueType"></param>
	public static void readLength(MethodGenerator g, String dtoMemberName) {
//		var method =MethodUtil.resolveMemoized(IDTOReader.class, "readLength", _readArgs)

		var prmIndex = SerializationArgs.ReaderIndex;
		g.invoke(prmIndex, "readLength", () -> {
			g.load(dtoMemberName);
		});
	}

	public static void readElement(MethodGenerator g, String dtoMemberName, IVariable index) {
		var prmIndex = SerializationArgs.ReaderIndex;
		g.invoke(prmIndex, "readElement", () -> {
			g.load(dtoMemberName);
			g.load(index);
		});
	}

}