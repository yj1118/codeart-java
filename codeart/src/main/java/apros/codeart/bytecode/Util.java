package apros.codeart.bytecode;

import static apros.codeart.i18n.Language.strings;

import org.objectweb.asm.Opcodes;

final class Util {

    private Util() {

    }

    static int getLoadArrayElementCode(Class<?> type) {
        if (!type.isPrimitive())
            return Opcodes.AALOAD;
        else {
            if (type == int.class)
                return Opcodes.IALOAD;
            else if (type == long.class)
                return Opcodes.LALOAD;
            else if (type == float.class)
                return Opcodes.FALOAD;
            else if (type == double.class)
                return Opcodes.DALOAD;
            else if (type == short.class)
                return Opcodes.SALOAD;
            else if (type == byte.class)
                return Opcodes.BALOAD;
            else if (type == boolean.class)
                return Opcodes.BALOAD;
            else if (type == char.class)
                return Opcodes.CALOAD;

            throw new IllegalArgumentException(strings("apros.codeart", "UnknownException"));
        }
    }

    static int getLoadCode(Class<?> type) {
        if (!type.isPrimitive())
            return Opcodes.ALOAD;
        else {
            if (type == int.class)
                return Opcodes.ILOAD;
            else if (type == long.class)
                return Opcodes.LLOAD;
            else if (type == float.class)
                return Opcodes.FLOAD;
            else if (type == double.class)
                return Opcodes.DLOAD;
            else if (type == short.class)
                return Opcodes.ILOAD;
            else if (type == byte.class)
                return Opcodes.ILOAD;
            else if (type == boolean.class)
                return Opcodes.ILOAD;
            else if (type == char.class)
                return Opcodes.ILOAD;

            throw new IllegalArgumentException(strings("apros.codeart", "UnknownException"));
        }
    }

    static int getStoreCode(Class<?> type) {
        if (!type.isPrimitive())
            return Opcodes.ASTORE;
        else {
            if (type == int.class) {
                return Opcodes.ISTORE;
            } else if (type == boolean.class) {
                return Opcodes.ISTORE;
            } else if (type == byte.class) {
                return Opcodes.ISTORE;
            } else if (type == float.class) {
                return Opcodes.FSTORE;
            } else if (type == long.class) {
                return Opcodes.LSTORE;
            } else if (type == double.class) {
                return Opcodes.DSTORE;
            } else if (type == short.class) {
                return Opcodes.ISTORE;
            } else if (type == char.class) {
                return Opcodes.ISTORE;
            }

            throw new IllegalArgumentException(strings("apros.codeart", "UnknownException"));
        }
    }

    /**
     * 将变量存入数组时要用到的代码
     *
     * @param type
     * @return
     */
    static int getStoreArrayCode(Class<?> type) {
        if (!type.isPrimitive())
            return Opcodes.AASTORE;
        else {
            if (type == int.class) {
                return Opcodes.IASTORE;
            } else if (type == boolean.class) {
                return Opcodes.BASTORE;
            } else if (type == byte.class) {
                return Opcodes.BASTORE;
            } else if (type == float.class) {
                return Opcodes.FASTORE;
            } else if (type == long.class) {
                return Opcodes.LASTORE;
            } else if (type == double.class) {
                return Opcodes.DASTORE;
            } else if (type == short.class) {
                return Opcodes.SASTORE;
            } else if (type == char.class) {
                return Opcodes.CASTORE;
            }

            throw new IllegalArgumentException(strings("apros.codeart", "UnknownException"));
        }
    }
}
