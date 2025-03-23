package apros.codeart.ddd.repository;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.StatusEventType;

public class RepositoryEventArgs {

    private final IAggregateRoot _target;

    /**
     * 需要执行仓储操作的领域对象
     *
     * @return
     */
    public IAggregateRoot target() {
        return _target;
    }

    private final StatusEventType _eventType;

    public StatusEventType eventType() {
        return _eventType;
    }

    public RepositoryEventArgs(IAggregateRoot target, StatusEventType eventType) {
        _target = target;
        _eventType = eventType;
    }
}
