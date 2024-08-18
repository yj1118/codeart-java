package apros.codeart.ddd.launcher;

import apros.codeart.PostApplicationStart;
import apros.codeart.PreApplicationStart;
import apros.codeart.RunPriority;
import apros.codeart.ddd.saga.internal.EventHost;

@PostApplicationStart(method = "initialized", priority = RunPriority.Framework_Medium)
public class PostStart {
    public static void initialized() {
        DomainHost.initialized();
    }
}
