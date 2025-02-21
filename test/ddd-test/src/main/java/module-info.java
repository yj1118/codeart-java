module ddd_test {
    requires com.google.common;
    requires apros.codeart;
    requires transitive apros.codeart.ddd;

    exports subsystem.account;
    exports subsystem.saga;

    opens subsystem.account;
    opens subsystem.saga;
}