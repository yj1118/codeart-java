module apros.codeart {
	requires org.objectweb.asm;
	requires com.google.common;
	requires org.reflections;

	exports apros.codeart;
	exports apros.codeart.i18n;
	exports apros.codeart.runtime;
	exports apros.codeart.context;
	exports apros.codeart.pooling;
	exports apros.codeart.pooling.util;
	exports apros.codeart.util;
	exports apros.codeart.dto;
	exports apros.codeart.dto.serialization;
	exports apros.codeart.mq;
	exports apros.codeart.mq.event;
	exports apros.codeart.mq.rpc.server;
	exports apros.codeart.mq.rpc.client;

	opens apros.codeart;
}