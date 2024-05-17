module apros.codeart {
	requires transitive org.objectweb.asm;
	requires com.google.common;
	requires org.reflections;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires transitive com.rabbitmq.client;

	exports apros.codeart;
	exports apros.codeart.log;
	exports apros.codeart.io;
	exports apros.codeart.i18n;
	exports apros.codeart.runtime;
	exports apros.codeart.context;
	exports apros.codeart.pooling;
	exports apros.codeart.pooling.util;
	exports apros.codeart.util;
	exports apros.codeart.util.concurrent;
	exports apros.codeart.util.thread;
	exports apros.codeart.bytecode;
	exports apros.codeart.dto;
	exports apros.codeart.dto.serialization;
	exports apros.codeart.echo;
	exports apros.codeart.echo.event;
	exports apros.codeart.echo.rpc;
	exports apros.codeart.rabbitmq;
	exports apros.codeart.rabbitmq.rpc;
	exports apros.codeart.rabbitmq.event;

	opens apros.codeart;
}