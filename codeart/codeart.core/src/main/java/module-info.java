module codeart.core {
	requires org.objectweb.asm;
	requires com.google.common;

	exports com.apros.codeart.core;
	exports com.apros.codeart.i18n;
	exports com.apros.codeart.runtime;
	exports com.apros.codeart.bytecode;
	exports com.apros.codeart.context;
	exports com.apros.codeart.util;
}