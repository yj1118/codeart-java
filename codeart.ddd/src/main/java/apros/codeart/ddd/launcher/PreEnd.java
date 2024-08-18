package apros.codeart.ddd.launcher;

import apros.codeart.PreApplicationEnd;
import apros.codeart.RunPriority;

@PreApplicationEnd(method = "dispose", priority = RunPriority.Framework_Medium)
public class PreEnd {
    public static void dispose() {
        DomainHost.dispose();
    }
}