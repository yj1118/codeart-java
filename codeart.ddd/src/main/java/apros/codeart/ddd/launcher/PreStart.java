package apros.codeart.ddd.launcher;

import apros.codeart.PreApplicationStart;

@PreApplicationStart("initialize")
public class PreStart {
    public static void initialize() {
        DomainHost.initialize();
    }
}
