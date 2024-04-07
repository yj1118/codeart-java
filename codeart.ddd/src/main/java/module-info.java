module codeart.ddd {
	requires com.google.common;
	requires transitive java.sql;
	requires com.zaxxer.hikari;
	requires transitive codeart;

	exports com.apros.codeart.ddd;
	exports com.apros.codeart.ddd.repository;
	exports com.apros.codeart.ddd.repository.access;
	exports com.apros.codeart.ddd.metadata;
	exports com.apros.codeart.ddd.launcher;
	exports com.apros.codeart.ddd.console;

	opens codeart.ddd;
}