package apros.codeart.dto.serialization.internal;

import static apros.codeart.runtime.Util.propagate;

import apros.codeart.bytecode.ClassGenerator;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.dto.IDTOReader;
import apros.codeart.util.GUID;

final class DTODeserializeMethodGenerator {

    private DTODeserializeMethodGenerator() {
    }

    public static DeserializeMethod generateMethod(TypeSerializationInfo typeInfo) {

        String methodName = String.format("DTODeserialize_%s", GUID.compact());

        try (var cg = ClassGenerator.define()) {

            try (var mg = cg.defineMethodPublicStatic(methodName, void.class, (args) -> {
                args.add(SerializationArgs.InstanceName, typeInfo.getTargetClass());
                args.add("reader", IDTOReader.class);
            })) {
                readMembers(mg, typeInfo);
            }

            //cg.save();

            // 返回生成的字节码
            var cls = cg.toClass();

            var method = cls.getDeclaredMethod(methodName, typeInfo.getTargetClass(), IDTOReader.class);

            return new DeserializeMethod(method);

        } catch (Throwable e) {
            throw propagate(e);
        }

    }

    private static void readMembers(MethodGenerator g, TypeSerializationInfo typeInfo) {
        for (var member : typeInfo.getMemberInfos()) {
            if (member.canWrite()) {
                member.generateDeserializeIL(g);
            }
        }
    }
}
