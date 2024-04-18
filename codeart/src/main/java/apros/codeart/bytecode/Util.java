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

			throw new IllegalArgumentException(strings("codeart", "UnknownException"));
		}
	}
}
