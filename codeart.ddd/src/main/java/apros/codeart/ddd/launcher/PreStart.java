package apros.codeart.ddd.launcher;

import apros.codeart.PreApplicationStart;
import apros.codeart.RunPriority;

@PreApplicationStart(method = "initialize", priority = RunPriority.Framework_Medium)
public class PreStart {
    public static void initialize() {
        DomainHost.initialize();
    }
}
