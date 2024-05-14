package apros.codeart.bytecode;

import static apros.codeart.i18n.Language.strings;

import org.objectweb.asm.Opcodes;

final class Util {

	private Util() {

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

			throw new IllegalArgumentException(strings("codeart", "UnknownException"));
		}
	}

	static int getStoreCode(Class<?> type) {
		if (!type.isPrimitive())
			return Opcodes.ASTORE;
		else {
			if (type == int.class) {
				return Opcodes.IASTORE;
			} else if (type == boolean.class) {
				return Opcodes.ISTORE;
			} else if (type == byte.class) {
				return Opcodes.ISTORE;
			} else if (type == float.class) {
				return Opcodes.FASTORE;
			} else if (type == long.class) {
				return Opcodes.LASTORE;
			} else if (type == double.class) {
				return Opcodes.DASTORE;
			} else if (type == short.class) {
				return Opcodes.ISTORE;
			} else if (type == char.class) {
				return Opcodes.ISTORE;
			}

			throw new IllegalArgumentException(strings("codeart", "UnknownException"));
		}
	}
}
