package apros.codeart.ddd.saga.internal.trigger;

import apros.codeart.ddd.saga.DomainEvent;

/**
 * {@name 条目名称，也就是对应的事件名称}
 * <p>
 * {@local 条目对应的本地事件，如果为null则为远程事件}
 * <p>
 * {@expanded 是否在队列中已将整个事件展开，达到可以执行的准备状态}
 * <p>
 * {@source 事件来源，即：是由哪个DomainEvent定义的}
 */
public record EventEntry(String name, DomainEvent local, boolean expanded, DomainEvent source) {

    public boolean isLocal() {
        return this.local != null;
    }

}
