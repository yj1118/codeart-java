package apros.codeart.echo.event;

import apros.codeart.App;
import apros.codeart.PreApplicationStart;

@PreApplicationStart("initialize")
public class PreStart {
    public static void initialize() {
        App.setup("echo.event");
    }
}