package apros.codeart.ddd;

import apros.codeart.ddd.repository.ScheduledActionType;

/**
 * 每个领域对象具有验证固定规则的能力
 */
public interface ISupportFixedRules {

    /**
     * 验证对象是否满足固定规则
     *
     * @return
     */
    ValidationResult validate(ScheduledActionType actionType);
}
