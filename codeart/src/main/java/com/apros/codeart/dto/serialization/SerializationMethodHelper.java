package com.apros.codeart.dto.serialization;

import static com.apros.codeart.i18n.Language.strings;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Consumer;

import com.apros.codeart.bytecode.MethodGenerator;
import com.apros.codeart.runtime.MethodUtil;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ReaderWriterLockSlim;

/**
 * 封装了根据类型获取序列化、反序列化的方法信息
 */
final class SerializationMethodHelper {

	private SerializationMethodHelper() {
	}

	private static final HashMap<Class<?>, Method> _serializeMethods = new HashMap<Class<?>, Method>();

	private static final HashMap<Class<?>, Method> _deserializeMethods = new HashMap<Class<?>, Method>();

	private static HashMap<Class<?>, Method> getMethods(SerializationMethodType methodType) {
		return methodType == SerializationMethodType.Serialize ? _serializeMethods : _deserializeMethods;
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
		case SerializationMethodType.Serialize:

			if (type.isEnum()) {
				method = MethodUtil.resolveMemoized(IDTOWriter.class, "writeEnum", String.class, Enum.class);
			} else {
				method = MethodUtil.resolveMemoized(IDTOWriter.class, "write", String.class, type);
				if ((method == null) && (!type.isPrimitive())) {
					// 如果不是int、long等基础类型，而有可能是自定义类型，那么用以下代码得到方法
					method = MethodUtil.resolveMemoized(IDTOWriter.class, "write", _writeObjectArgs);
				}
			}

			break;
		case SerializationMethodType.Deserialize:
			if (type.isEnum()) {
				method = MethodUtil.resolveMemoized(IDTOReader.class, "readEnum", String.class);
			} else {
				String methodName = String.format("read{0}", type.getName());
				method = MethodUtil.resolveMemoized(IDTOReader.class, methodName, _readArgs);
				if ((method == null) && (!type.isPrimitive())) {
					// 如果不是int、long等基础类型，而有可能是自定义类型，那么用以下代码得到方法
					// IDTOWriter.ReadObject<T>(string name);
					method = MethodUtil.resolveMemoized(IDTOReader.class, "readObject", _readArgs);
				}
			}

			break;
		}

		if (method == null) {
			throw new IllegalArgumentException(strings("NotFoundDTOSerializationMethod", TypeUtil.resolveName(type)));
		}

		return method;
	}

	private static final Class<?>[] _writeObjectArgs = new Class<?>[] { String.class, Object.class };
	private static final Class<?>[] _readArgs = new Class<?>[] { String.class };

//	#endregion

	/// <summary>
	/// 根据类型得到序列化方法
	/// </summary>
	/// <param name="type"></param>
	/// <param name="methodType"></param>
	/// <returns></returns>
	public static Method getTypeMethod(Class<?> type, SerializationMethodType methodType) {
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
			if (typeName == DTObjectSerializer.class.getName())
				return SerializationArgs.SerializerIndex;
			if (typeName == IDTOWriter.class.getName())
				return SerializationArgs.WriterIndex;
		} else {
			if (typeName == DTObjectDeserializer.class.getName())
				return SerializationArgs.DeserializerIndex;
			if (typeName == IDTOReader.class.getName())
				return SerializationArgs.ReaderIndex;
		}
		throw new IllegalArgumentException(strings("NotFoundParameterIndex", typeName));
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
		g.invoke(method.getName(), () -> {
			g.loadParameter(prmIndex);
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

//
//	public static bool IsPrimitive(Type type)
//  {
//      return type == typeof(string)
//          || type == typeof(int)
//          || type == typeof(long)
//          || type == typeof(DateTime)
//          || type == typeof(Guid)
//          || type == typeof(float)
//          || type == typeof(double)
//          || type == typeof(uint)
//          || type == typeof(ulong)
//          || type == typeof(ushort)
//          || type == typeof(sbyte)
//          || type == typeof(char)
//          || type == typeof(byte)
//          || type == typeof(bool)
//          || type == typeof(decimal)
//          || type == typeof(short);
//  }
//
//	public static void WriteBlob(MethodGenerator g, string dtoMemberName, Action loadValue)
//  {
//      var method = typeof(IDTOWriter).ResolveMethod("WriteBlob",
//                                                      new Type[] { typeof(string), typeof(byte[]) });
//      var prmIndex = SerializationArgs.WriterIndex;
//      g.Call(method, () =>
//      {
//          g.LoadParameter(prmIndex);
//          g.Load(dtoMemberName);
//          loadValue();
//      });
//  }

	public static void writeElement(MethodGenerator g, String dtoMemberName, Class<?> elementType, Runnable loadValue) {

//      var method =  typeof(IDTOWriter).ResolveMethod("writeElement",
//                                                      new Type[] { elementType },
//                                                      MethodParameter.Create<string>(), 
//                                                      MethodParameter.Create<bool>(),
//                                                      MethodParameter.CreateGeneric(elementType));
		var prmIndex = SerializationArgs.WriterIndex;

		g.invoke(prmIndex, dtoMemberName, () -> {
			g.load(dtoMemberName);
			loadValue.run();
		});
	}

	public static void writeArray(MethodGenerator g, String dtoMemberName) {
		var method = MethodUtil.resolveMemoized(IDTOWriter.class, "writeArray", String.class);
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
		g.invoke(method.getName(), () -> {
			g.loadParameter(prmIndex);
			g.load(dtoMemberName);
		});
	}
//
//	public static void ReadBlob(MethodGenerator g, string dtoMemberName)
//  {
//      var method = typeof(IDTOReader).ResolveMethod("ReadBlob", typeof(string));
//      var prmIndex = SerializationArgs.ReaderIndex;
//      g.Call(method, () =>
//      {
//          g.LoadParameter(prmIndex);
//          g.Load(dtoMemberName);
//      });
//  }
//
//	/// <summary>
//	/// 读取数组的长度
//	/// </summary>
//	/// <param name="g"></param>
//	/// <param name="dtoMemberName"></param>
//	/// <param name="valueType"></param>
//	public static void ReadLength(MethodGenerator g, string dtoMemberName)
//  {
//      var method = typeof(IDTOReader).ResolveMethod("ReadLength", _readArgs);
//      var prmIndex = SerializationArgs.ReaderIndex;
//      g.Call(method, () =>
//      {
//          g.LoadParameter(prmIndex);
//          g.Load(dtoMemberName);
//      });
//  }
//
//	public static void ReadElement(MethodGenerator g, string dtoMemberName,Type elementType, IVariable index)
//  {
//      var method = typeof(IDTOReader).ResolveMethod("ReadElement", new Type[] { elementType }, MethodParameter.Create<string>(), MethodParameter.Create<int>());
//      var prmIndex = SerializationArgs.ReaderIndex;
//      g.Call(method, () =>
//      {
//          g.LoadParameter(prmIndex);
//          g.Load(dtoMemberName);
//          g.LoadVariable(index, LoadOptions.Default);
//      });
//  }

}