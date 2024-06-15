package apros.codeart.ddd.launcher;

import apros.codeart.App;
import apros.codeart.IAppInstaller;
import apros.codeart.ddd.DDDConfig;

/**
 * 用于测试的启动器
 */
public final class TestLauncher {

    private TestLauncher() {
    }

    public static void start(LauncherConfig config) {
        start(new AppInstaller(), config);
    }

    public static void start() {
        start(new AppInstaller(), LauncherConfig.Test);
    }

    public static void start(IAppInstaller installer, LauncherConfig config) {

        DDDConfig.enableReset(config.enableReset());

        // 要从框架/子系统/服务宿主 3大块里找定义
        App.init(installer);

        App.inited();
    }

    public static void stop() {
        App.dispose();
        App.disposed();
    }
}
