package apros.codeart.ddd.launcher;

public final class LauncherConfig {

    private final boolean _enableReset;

    public boolean enableReset() {
        return _enableReset;
    }

    public LauncherConfig(boolean enableReset) {
        _enableReset = enableReset;
    }

    public static final LauncherConfig Test = new LauncherConfig(true);

}
