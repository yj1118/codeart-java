package apros.codeart.ddd.saga.internal.protector;

import apros.codeart.ddd.saga.RaisedEntry;
import apros.codeart.util.StringUtil;

class Trace {

    private String _queueId;

    public String queueId() {
        return _queueId;
    }

    private final StringBuilder _code;

    public Trace() {
        _code = new StringBuilder();
    }

    public void start(String queueId) {
        _code.setLength(0);
        _queueId = queueId;
        _code.append(String.format("--- queueId:%s ---", _queueId));
        StringUtil.appendLine(_code);
    }

    /**
     * 开始准备回溯事件
     *
     * @param event
     */
    public void start(RaisedEntry event) {
        _code.append("--- event-start ---");
        StringUtil.appendLine(_code);
        StringUtil.appendFormat(_code, "eventName:%s%s", event.index(), event.name());
        StringUtil.appendLine(_code);
    }

    /**
     * 回溯事件结束
     *
     * @param event
     */
    public void end(RaisedEntry event) {
        _code.append("--- event-end ---");
    }

    /**
     * 表示全部回溯完成
     */
    public void end() {
        _code.append("--- queue-end ---");
    }

    public void clear() {
        _code.setLength(0);
    }

}
