package apros.codeart.echo.rpc;

import apros.codeart.App;
import apros.codeart.PreApplicationStart;
import apros.codeart.RunPriority;

@PreApplicationStart(method = "initialize", priority = RunPriority.Framework_High)
public class PreStart {
    public static void initialize() {
        App.setup("echo.rpc");
    }
}