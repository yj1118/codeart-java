module apros.codeart.ddd {
    requires com.google.common;
    requires transitive java.sql;
    requires com.zaxxer.hikari;
    requires transitive apros.codeart;
    requires jsqlparser;
    requires org.jetbrains.annotations;
    requires java.xml.crypto;
    requires java.management;
    requires org.postgresql.jdbc;

    exports apros.codeart.ddd;
    exports apros.codeart.ddd.validation;
    exports apros.codeart.ddd.command;
    exports apros.codeart.ddd.repository;
    exports apros.codeart.ddd.repository.access;
    exports apros.codeart.ddd.metadata;
    exports apros.codeart.ddd.virtual;
    exports apros.codeart.ddd.message;
    exports apros.codeart.ddd.launcher;
    exports apros.codeart.ddd.service;
    exports apros.codeart.ddd.toolkit;
    exports apros.codeart.ddd.toolkit.tree;

    exports apros.codeart.ddd.saga;

    opens apros.codeart.ddd;
    opens apros.codeart.ddd.validation;
//    exports apros.codeart.ddd.metadata.internal;
    opens apros.codeart.ddd.metadata.internal;
    exports apros.codeart.ddd.saga.internal.trigger;
//    exports apros.codeart.ddd.virtual.internal;
}