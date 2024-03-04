package com.apros.codeart.dto.serialization;

final class DTODeserializeMethodGenerator {

	private DTODeserializeMethodGenerator() {
	}

	public static DeserializeMethod generateMethod(TypeSerializationInfo typeInfo) {
		DynamicMethod method = new DynamicMethod(string.Format("DTODeserialize_{0}", Guid.NewGuid().ToString("n")),
				null, new Type[] { typeof(object), typeof(IDTOReader) }, true);

		MethodGenerator g = new MethodGenerator(method);

		DeclareInstance(g, typeInfo);
		ReadMembers(g, typeInfo);

		g.Return();

		return (DeserializeMethod) method.CreateDelegate(typeof(DeserializeMethod));
	}

	/// <summary>
	/// 声明强类型的instance变量留待后续代码使用，避免频繁类型转换
	/// </summary>
	private static void DeclareInstance(MethodGenerator g, TypeSerializationInfo typeInfo)
	{
	    //TypeClassName instance = (TypeClassName)instance;
	    var instance = g.Declare(typeInfo.ClassType, SerializationArgs.InstanceName);
	    g.Assign(instance, () =>
	    {
	        g.LoadParameter(SerializationArgs.InstanceIndex);
	        g.UnboxAny(typeInfo.ClassType);
	    });
	}

	private static void ReadMembers(MethodGenerator g, TypeSerializationInfo typeInfo)
	{
	    if (typeInfo.ClassAttribute.Mode == DTOSerializableMode.General)
	    {
	        foreach (var member in typeInfo.MemberInfos)
	        {
	            //只有可以写入并且不是抽象的成员才能从dto中赋值
	            if (member.CanWrite && !member.IsAbstract)
	            {
	                g.BeginScope();
	                member.GenerateDeserializeIL(g);
	                g.EndScope();
	            }
	        }
	    }
	    else
	    {
	        //在函数模式,只有标记了Parameter的成员才会被反序列化到对象实例中
	        foreach (var member in typeInfo.MemberInfos)
	        {
	            if (member.MemberAttribute.Type == DTOMemberType.Parameter && member.CanWrite && !member.IsAbstract)
	            {
	                g.BeginScope();
	                member.GenerateDeserializeIL(g);
	                g.EndScope();
	            }
	        }
	    }
	}
}
