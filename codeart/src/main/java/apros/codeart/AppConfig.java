package apros.codeart;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;
import apros.codeart.util.ResourceUtil;

public final class AppConfig {

    private AppConfig() {

    }

    private static Iterable<String> _archives;

    public static String[] mergeArchives(String... appendArchives) {

        if (appendArchives != null && appendArchives.length > 0) {
            return mergeArchives(archives(), appendArchives);
        }

        return Iterables.toArray(archives(), String.class);
    }

    /**
     * 需要参与初始化的档案名称，档案是包的顶级名称，比如 subsystem.account和subsystem.user的档案名为subsystem
     *
     * @return
     */
    public static Iterable<String> archives() {
        if (_archives == null) {
            _archives = ImmutableList.copyOf(_config.getStrings("archives", () -> ListUtil.empty()));
        }
        return _archives;
    }

    private static String[] mergeArchives(Iterable<String> source, String[] append) {
        if (append.length == 0)
            return Iterables.toArray(source, String.class);
        ArrayList<String> result = new ArrayList<String>();
        ListUtil.addRange(result, source, true);
        ListUtil.addRange(result, append, true); // 过滤重复项
        return Iterables.toArray(result, String.class);
    }

    public static DTObject section(String path) {
        return _config.getObject(path, null);
    }

    public static String getString(String path) {
        return _config.getString(path, null);
    }

    private static final DTObject _config;

    public static DTObject section() {
        return _config;
    }

    static {
        _config = ResourceUtil.loadJSON("codeart.json");
    }
}
