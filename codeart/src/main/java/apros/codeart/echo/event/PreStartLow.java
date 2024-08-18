package apros.codeart.echo.event;

import apros.codeart.App;
import apros.codeart.PreApplicationStart;
import apros.codeart.RunPriority;

@PreApplicationStart(method = "initialize", priority = RunPriority.Framework_Low)
public class PreStartLow {
    public static void initialize() {
        EventPortal.startUp();
    }
}