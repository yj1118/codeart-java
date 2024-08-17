package apros.codeart;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

import apros.codeart.runtime.Activator;
import apros.codeart.runtime.MethodUtil;
import apros.codeart.util.ArgumentAssert;
import apros.codeart.util.ListUtil;

public final class App {
    private App() {
    }

    private static String[] _archives;

    public static String[] archives() {
        return _archives;
    }

    private static IAppInstaller _installer;

    public static void setup(String moduleName) {
        _installer.setup(moduleName, null);
    }

    public static void setup(String moduleName, Object[] args) {
        _installer.setup(moduleName, args);
    }

    public static void init() {
        init(DefaultAppInstaller.Instance);
    }

    /**
     * 应用程序初始化，请根据不同的上下文环境，在程序入口处调用此方法
     */
    public static void init(IAppInstaller installer) {

        _archives = installer.getArchives();

        ArgumentAssert.isNotNullOrEmpty(_archives, "archives");

        installer.init();

        _installer = installer;

        process_pre_start();
    }

    private static void process_pre_start() {

        runActions(PreApplicationStart.class);
    }

    private static final AtomicBoolean _post_start_completed = new AtomicBoolean(false);

    public static boolean started() {
        return _post_start_completed.getAcquire();
    }

    /// <summary>
    /// 应用程序初始化完后，请根据不同的上下文环境，在程序入口处调用此方法
    /// </summary>
    public static void inited() {

        process_post_start();
        // 清理安装器的资源
        _installer.dispose();

        _post_start_completed.setRelease(true);
    }

    private static void process_post_start() {
        runActions(PostApplicationStart.class);
    }

    public static void dispose() {
        process_pre_end();
    }

    private static void process_pre_end() {
        runActions(PreApplicationEnd.class);
    }

    public static void disposed() {
        process_post_end();
    }

    private static void process_post_end() {
        runActions(PostApplicationEnd.class);
    }

    private static void runActions(Class<? extends Annotation> annType) {
        var items = ListUtil.map(Activator.getAnnotatedTypesOf(annType, _archives), (type) -> {
            var ann = type.getAnnotation(annType);
            String methodName = (String) MethodUtil.invoke(annType, "method", ann);
            RunPriority priority = (RunPriority) MethodUtil.invoke(annType, "priority", ann);
            return new ActionItem(type, methodName, priority);
        });

        items.sort((t1, t2) -> {
            return t2.priority().getValue() - t1.priority().getValue();
        });

        for (var item : items) {
            item.run();
        }
    }

    private static class DefaultAppInstaller extends AppInstallerBase {

        private DefaultAppInstaller() {
        }

        @Override
        public void init() {
            // TODO Auto-generated method stub

        }

        @Override
        public String[] getArchives() {
            return AppConfig.mergeArchives("apros.codeart");
        }

        @Override
        public void setup(String moduleName, Object[] args) {

        }

        @Override
        public void dispose() {

        }

        public static final DefaultAppInstaller Instance = new DefaultAppInstaller();

    }

}
