module codeart {
	requires org.objectweb.asm;
	requires com.google.common;
	requires org.reflections;
	requires org.agrona.core;

	exports apros.codeart;
	exports apros.codeart.i18n;
	exports apros.codeart.runtime;
	exports apros.codeart.context;
	exports apros.codeart.util;
	exports apros.codeart.dto;
	exports apros.codeart.dto.serialization;

	opens codeart;
}