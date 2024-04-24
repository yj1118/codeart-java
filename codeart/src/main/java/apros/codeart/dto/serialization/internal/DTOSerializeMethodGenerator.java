package apros.codeart.dto.serialization.internal;

import static apros.codeart.runtime.Util.propagate;

import apros.codeart.bytecode.ClassGenerator;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.dto.IDTOWriter;
import apros.codeart.util.Guid;

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

		String methodName = String.format("DTOSerialize_%s", Guid.compact());

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
		for (var member : typeInfo.getMemberInfos()) {
			if (member.canRead()) {
				member.generateSerializeIL(g);
			}

		}
	}

}
