module codeart.ddd {
	requires com.google.common;
	requires transitive java.sql;
	requires com.zaxxer.hikari;
	requires codeart;

	exports com.apros.codeart.ddd;
	exports com.apros.codeart.ddd.repository;
	exports com.apros.codeart.ddd.repository.access;
	exports com.apros.codeart.ddd.launcher;
	exports com.apros.codeart.ddd.console;

	opens codeart.ddd;
}