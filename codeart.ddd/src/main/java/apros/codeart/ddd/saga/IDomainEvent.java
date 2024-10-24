package apros.codeart.ddd.saga;

import apros.codeart.dto.DTObject;

public interface IDomainEvent {

    /**
     * 触发领域事件
     *
     * @param arg 接收到的参数
     * @return 返回的值，当回溯的时候，这个返回值会作为参数传递给当前事件
     */
    DTObject raise(DTObject arg, EventContext context);

    /**
     * 回溯事件
     *
     * @return
     */
    void reverse(DTObject arg);

}
