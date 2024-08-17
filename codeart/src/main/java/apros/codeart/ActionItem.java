package apros.codeart;

import static apros.codeart.i18n.Language.strings;
import static apros.codeart.runtime.Util.propagate;

import apros.codeart.runtime.MethodUtil;

public record ActionItem(Class<?> type, String methodName, RunPriority priority) {

    public void run() {

        var method = MethodUtil.resolveByNameOnce(type, methodName);

        if (method == null) {
            throw new IllegalStateException(strings("apros.codeart", "NotFoundMethod", type.getName(), methodName));
        }

        if (!MethodUtil.isStatic(method) || method.getParameterCount() > 0 || !MethodUtil.isPublic(method))
            throw new IllegalStateException(strings("apros.codeart", "LifeMethodError", type.getName(), methodName));

        try {
            method.invoke(null);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }
}