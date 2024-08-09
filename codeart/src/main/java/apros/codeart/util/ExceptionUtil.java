package apros.codeart.util;

import static apros.codeart.runtime.Util.propagate;

import java.io.PrintWriter;
import java.io.StringWriter;

import apros.codeart.UIException;
import apros.codeart.pooling.util.StringPool;

public final class ExceptionUtil {
    private ExceptionUtil() {

    }

    public static String fullStackTrace(Exception ex) {
        if (UIException.is(ex))
            return StringUtil.empty();
        try (var sw = new StringWriter()) {
            try (var pw = new PrintWriter(sw)) {
                ex.printStackTrace(pw);
                return sw.toString();
            }
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    public static String fullMessage(Exception ex) {
        return StringPool.using((code) -> {
            fillMessage(ex, code);
        });

    }

    private static void fillMessage(Exception ex, StringBuilder code) {
        if (ex == null)
            return;
        if (UIException.is(ex))
            code.append(ex.getMessage());

        while (true) {
            code.append(ex.getMessage());

            if (ex.getCause() == null) {
                break;
            }

            StringUtil.appendLine(code);
            ex = (Exception) ex.getCause();
        }
    }

    /**
     * 获得异常的完整信息
     *
     * @param ex
     * @return
     */
    public static String full(Exception ex) {

        return StringPool.using((code) -> {
            fillMessage(ex, code);
            StringUtil.appendLine(code);
            code.append(fullStackTrace(ex));
        });
    }

}
