package apros.codeart.ddd.saga;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import apros.codeart.ddd.saga.internal.EventLog;
import apros.codeart.ddd.saga.internal.EventUtil;
import apros.codeart.dto.DTObject;

public final class EventContext {

    private final String _id;

    public String id() {
        return _id;
    }

    private String _eventName;

    public String eventName() {
        return _eventName;
    }

    /**
     * 参数是会在多个事件之间传递的，而事件日志则是针对当前事件独立存储的，不会传递
     * 日志专用于回溯
     */
    private DTObject _log;

    private final DTObject _input;

    /**
     * 事件最初的输入
     *
     * @return
     */
    public DTObject input() {
        return _input;
    }


    public EventContext(String id, DTObject input) {
        _id = id;
        _input = input;
    }


    private String _eventId;

    public String eventId() {
        return _eventId;
    }

    private int _index;

    public int index() {
        return _index;
    }

    /**
     * @param eventName
     * @param index     事件被执行的序号
     */
    void direct(String eventName, int index) {
        _eventName = eventName;
        _index = index;
        _eventId = EventUtil.getEventId(this.id(), _eventName, this.index());
        _log = null;
    }

    /**
     * 获得要执行事件得远程数据
     *
     * @param args
     * @return
     */
    DTObject getEntryRemotable(DTObject args) {
        var e = DTObject.editable();
        e.setString("id", this.id()); // 环境编号（也是队列编号）
        e.setString("eventId", this.eventId());
        e.setString("eventName", this.eventName());
        e.setObject("args", args);
        return e;
    }

    public void submit(Consumer<DTObject> action) {
        if (_log == null)
            _log = DTObject.editable();

        action.accept(_log);
        EventLog.writeRaiseLog(_id, _eventName, _index, _log);
    }

    private boolean _isPropagationStopped;

    public boolean isPropagationStopped() {
        return _isPropagationStopped;
    }

    /**
     * 提前阻止事件执行
     */
    public void stopPropagation() {
        _isPropagationStopped = true;
    }
}
