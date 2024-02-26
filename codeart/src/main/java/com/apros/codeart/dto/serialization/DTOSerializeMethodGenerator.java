//package com.apros.codeart.dto.serialization;
//
///**
// * 序列化类型的动态方法生成器（自动序列化）
// */
//class DTOSerializeMethodGenerator {
//	private DTOSerializeMethodGenerator() {
//	}
//
//	/// <summary>
//	///
//	/// </summary>
//	/// <param name="properties"></param>
//	public static SerializeMethod generateMethod(TypeSerializationInfo typeInfo) {
//		return null;
////		DynamicMethod method = new DynamicMethod(string.Format("DTOSerialize_{0}", Guid.NewGuid().ToString("n")), null,
////				new Type[] { typeof(object), typeof(IDTOWriter) }, true);
////
////		MethodGenerator g = new MethodGenerator(method);
////
////		DeclareInstance(g, typeInfo);
////		WriteMembers(g, typeInfo);
////
////		g.Return();
////
////		return (SerializeMethod) method.CreateDelegate(typeof(SerializeMethod));
//
////		method.invoke(null, "Hello", 123); // 静态方法无需实例化对象，传入 null 即可
//
////		try {
////			String guid = StringUtil.uuid();
////
////			// 创建 ByteBuddy 实例
////			ByteBuddy byteBuddy = new ByteBuddy();
////
////			var imple = new MethodImplementation(typeInfo);
////
////			Class<?> dynamicType = byteBuddy.subclass(Object.class)
////					.name(String.format("com.apros.codeart.dto.serialization.dynamic.DTOSerializer_%s", guid))
////					.defineMethod("serialize", void.class, Modifier.PUBLIC + Modifier.STATIC)
////					.withParameters(typeInfo.getTargetClass(), IDTOWriter.class).intercept(imple).make()
////					.load(DTOSerializeMethodGenerator.class.getClassLoader()).getLoaded();
////
////			// 获取新定义的静态方法并调用
////			var method = dynamicType.getDeclaredMethod("serialize", Object.class, IDTOWriter.class);
////			return new SerializeMethod(method);
////		} catch (Exception e) {
////			throw propagate(e);
////		}
//
//	}
//
////	/// <summary>
////	/// 声明强类型的instance变量留待后续代码使用，避免频繁类型转换
////	/// </summary>
////	private static void DeclareInstance(MethodGenerator g, TypeSerializationInfo typeInfo)
////	 {
////	     //TypeClassName instance = (TypeClassName)instance;
////	     var instance = g.Declare(typeInfo.ClassType, SerializationArgs.InstanceName);
////	     g.Assign(instance, () =>
////	     {
////	         g.LoadParameter(SerializationArgs.InstanceIndex);
////	         g.UnboxAny(typeInfo.ClassType);
////	     });
////	 }
////
////	private static void WriteMembers(MethodGenerator g, TypeSerializationInfo typeInfo)
////	 {
////	     if (typeInfo.ClassAttribute.Mode == DTOSerializableMode.General)
////	     {
////	         foreach (var member in typeInfo.MemberInfos)
////	         {
////	             if(member.CanRead)
////	             {
////	                 g.BeginScope();
////	                 member.GenerateSerializeIL(g);
////	                 g.EndScope();
////	             }
////	         }
////	     }
////	     else
////	     {
////	         //在函数模式,只有标记了ReturnValue的成员才会被写入到dto中
////	         foreach (var member in typeInfo.MemberInfos)
////	         {
////	             if(member.MemberAttribute.Type == DTOMemberType.ReturnValue && member.CanRead)
////	             {
////	                 g.BeginScope();
////	                 member.GenerateSerializeIL(g);
////	                 g.EndScope();
////	             }
////	         }
////	     }
////	 }
//
//}
