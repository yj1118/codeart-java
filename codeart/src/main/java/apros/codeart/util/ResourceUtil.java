package apros.codeart.util;

import static apros.codeart.runtime.Util.propagate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import apros.codeart.dto.DTObject;

public final class ResourceUtil {

    private ResourceUtil() {
    }

    /**
     * @param path
     * @return
     */
    public static String load(String path) {
        // 获取类加载器
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // 使用 try-with-resources 语句确保 InputStream 和 BufferedReader 被关闭
        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {

            if (inputStream == null) {
                return null;
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            }
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    public static DTObject loadJSON(String path) {
        var code = ResourceUtil.load(path);
        if (code == null)
            return DTObject.Empty;
        return DTObject.readonly(code);
    }

}
