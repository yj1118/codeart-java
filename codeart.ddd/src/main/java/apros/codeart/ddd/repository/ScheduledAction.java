package apros.codeart.ddd.repository;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.ValidationResult;

class ScheduledAction {

    private final IAggregateRoot _target;

    public IAggregateRoot target() {
        return _target;
    }

    private final ScheduledActionType _type;

    public ScheduledActionType type() {
        return _type;
    }

    private final IPersistRepository _repository;

    /**
     * 显示指定target的映射类型
     * <p>
     * 避免继承造成的影响
     *
     * @return
     */
    public IPersistRepository repository() {
        return _repository;
    }

    private boolean _expired;

    public boolean expired() {
        return _expired;
    }

    public ScheduledAction(IAggregateRoot target, IPersistRepository repository, ScheduledActionType type) {
        _target = target;
        _repository = repository;
        _type = type;
    }

    /**
     * 标示该行为已过期（也就是已执行过）
     */
    public void markExpired() {
        _expired = true;
    }

    /**
     * 固定规则验证
     *
     * @return
     */
    public ValidationResult validate() {
        return this.target().validate(this.type());
    }

}
