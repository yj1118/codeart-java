module ddd_test {
    requires com.google.common;
    requires apros.codeart;
    requires transitive apros.codeart.ddd;

    exports subsystem.account;

    opens subsystem.account;
}