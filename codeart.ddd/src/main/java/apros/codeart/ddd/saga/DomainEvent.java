package apros.codeart.ddd.saga;

import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;

/**
 * 领域事件的设计不能依赖与上下文数据，比如：当前登录者身份等
 * <p>
 * 如果有需要，就将上下文数据以参数形式传递给事件
 */
public abstract class DomainEvent implements IDomainEvent {

    public DomainEvent() {
    }

    /**
     * 事件名称，在一个项目内全局唯一
     *
     * @return
     */
    public abstract String name();

    /**
     * 前置事件
     *
     * @return
     */
    public Iterable<String> getPreEvents(DTObject input) {
        return ListUtil.empty();
    }

    /**
     * 后置事件
     *
     * @return
     */
    public Iterable<String> getPostEvents(DTObject input) {
        return ListUtil.empty();
    }

//    public abstract DTObject getArgs(DTObject args, EventContext ctx);

//	#endregion

    /**
     * 触发事件
     */
    public abstract DTObject raise(DTObject arg, EventContext ctx);

    /**
     * 回溯事件
     */
    public abstract void reverse(DTObject log);

}
