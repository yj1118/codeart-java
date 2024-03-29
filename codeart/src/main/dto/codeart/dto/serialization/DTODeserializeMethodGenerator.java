package com.apros.codeart.dto.serialization;

import static com.apros.codeart.runtime.Util.propagate;

import com.apros.codeart.bytecode.ClassGenerator;
import com.apros.codeart.bytecode.MethodGenerator;
import com.apros.codeart.util.StringUtil;

final class DTODeserializeMethodGenerator {

	private DTODeserializeMethodGenerator() {
	}

	public static DeserializeMethod generateMethod(TypeSerializationInfo typeInfo) {

		String methodName = String.format("DTODeserialize_%s", StringUtil.uuid());

		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic(methodName, void.class, (args) -> {
				args.add(SerializationArgs.InstanceName, typeInfo.getTargetClass());
				args.add("reader", IDTOReader.class);
			})) {
				readMembers(mg, typeInfo);
			}

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod(methodName, typeInfo.getTargetClass(), IDTOReader.class);

			return new DeserializeMethod(method);

		} catch (Exception e) {
			throw propagate(e);
		}

	}

	private static void readMembers(MethodGenerator g, TypeSerializationInfo typeInfo) {
		if (typeInfo.getClassAnn().mode() == DTOSerializableMode.General) {
			for (var member : typeInfo.getMemberInfos()) {
				// 只有可以写入并且不是抽象的成员才能从dto中赋值
				if (!member.isAbstract()) {
					member.generateDeserializeIL(g);
				}
			}
		} else {
			// 在函数模式,只有标记了Parameter的成员才会被反序列化到对象实例中
			for (var member : typeInfo.getMemberInfos()) {
				if (member.getMemberAnn().getType() == DTOMemberType.Parameter && !member.isAbstract()) {
					member.generateDeserializeIL(g);
				}
			}
		}
	}
}
