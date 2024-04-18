module codeart.ddd {
	requires com.google.common;
	requires transitive java.sql;
	requires com.zaxxer.hikari;
	requires transitive codeart;
	requires jsqlparser;

	exports apros.codeart.ddd;
	exports apros.codeart.ddd.repository;
	exports apros.codeart.ddd.repository.access;
	exports apros.codeart.ddd.metadata;
	exports apros.codeart.ddd.launcher;
	exports apros.codeart.ddd.console;

	opens codeart.ddd;
}