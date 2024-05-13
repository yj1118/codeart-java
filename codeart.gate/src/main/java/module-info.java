module apros.codeart.gate {
	requires com.google.common;
	requires io.vertx.core;
	requires io.vertx.eventbusbridge.common;
	requires io.vertx.auth.common;
	requires io.vertx.web;
	requires io.vertx.web.common;

	requires transitive apros.codeart;

	opens apros.codeart.gate;
}