package com.apros.codeart.dto.serialization;

import static com.apros.codeart.runtime.Util.propagate;

import com.apros.codeart.bytecode.ClassGenerator;
import com.apros.codeart.bytecode.MethodGenerator;
import com.apros.codeart.dto.IDTOWriter;
import com.apros.codeart.util.StringUtil;

/**
 * 序列化类型的动态方法生成器（自动序列化）
 */
final class DTOSerializeMethodGenerator {
	private DTOSerializeMethodGenerator() {
	}

	/// <summary>
	///
	/// </summary>
	/// <param name="properties"></param>
	public static SerializeMethod generateMethod(TypeSerializationInfo typeInfo) {

		String methodName = String.format("DTOSerialize_%s", StringUtil.uuid());

		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic(methodName, void.class, (args) -> {
				args.add(SerializationArgs.InstanceName, typeInfo.getTargetClass());
				args.add("writer", IDTOWriter.class);
			})) {
				writeMembers(mg, typeInfo);
			}

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod(methodName, typeInfo.getTargetClass(), IDTOWriter.class);

			return new SerializeMethod(method);

		} catch (Exception e) {
			throw propagate(e);
		}

	}

	private static void writeMembers(MethodGenerator g, TypeSerializationInfo typeInfo) {
		if (typeInfo.getClassAnn().mode() == DTOSerializableMode.General) {
			for (var member : typeInfo.getMemberInfos()) {
				member.generateSerializeIL(g);
			}
		} else {
			// 在函数模式,只有标记了ReturnValue的成员才会被写入到dto中
			for (var member : typeInfo.getMemberInfos()) {
				if (member.getMemberAnn().getType() == DTOMemberType.ReturnValue) {
					member.generateSerializeIL(g);
				}
			}
		}
	}

}
