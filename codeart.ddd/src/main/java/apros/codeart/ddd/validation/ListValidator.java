package apros.codeart.ddd.validation;

import apros.codeart.ddd.*;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.SafeAccess;
import com.google.common.collect.Iterables;

@SafeAccess
public class ListValidator extends PropertyValidatorImpl {

    public ListValidator(List tip) {
        super(tip);
    }

    public int min() {
        return this.getTip(List.class).min();
    }

    public int max() {
        return this.getTip(List.class).max();
    }

    public boolean validateItem() {
        return this.getTip(List.class).validateItem();
    }

    @Override
    protected void validate(DomainObject domainObject, PropertyMeta property, Object propertyValue,
                            ScheduledActionType actionType, ValidationResult result) {
        var list = TypeUtil.as(propertyValue, Iterable.class);

        if (list != null) {

            var tip = this.getTip(List.class);
            var count = Iterables.size(list);
            if (count < tip.min())
                result.append(property.name(), ListCountError, Language.strings("apros.codeart.ddd", "ListCountLessThan", property.call(), tip.min()));
            else if (count > tip.max())
                result.append(property.name(), ListCountError, Language.strings("apros.codeart.ddd", "ListCountMoreThan", property.call(), tip.max()));

            if (tip.validateItem()) {
                for (var item : list) {
                    ISupportFixedRules support = TypeUtil.as(item, ISupportFixedRules.class);
                    if (support != null) {
                        ValidationResult t = support.validate(actionType);
                        if (!t.isSatisfied())
                            result.append(property.name(), ListItemError, Language.strings("apros.codeart.ddd", "ListItemError", property.call(), t.getMessage()));
                    }
                }
            }

        }
    }

    public final String ListCountError = "ListCountError";
    public final String ListItemError = "ListItemError";

    @Override
    public DTObject getData() {
        // 请注意，数据格式要与注解的属性对应上
        DTObject data = DTObject.editable();
        data.setInt("min", this.min());
        data.setInt("max", this.max());
        data.setBoolean("validateItem", this.validateItem());
        return data;
    }
}
