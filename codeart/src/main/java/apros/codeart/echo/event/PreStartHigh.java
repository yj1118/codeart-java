package apros.codeart.echo.event;

import apros.codeart.App;
import apros.codeart.PreApplicationStart;
import apros.codeart.RunPriority;

@PreApplicationStart(method = "initialize", priority = RunPriority.Framework_High)
public class PreStartHigh {
    public static void initialize() {
        App.setup("echo.event");
    }
}