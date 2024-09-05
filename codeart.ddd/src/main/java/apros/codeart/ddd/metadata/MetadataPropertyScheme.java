package apros.codeart.ddd.metadata;

import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.dto.DTObject;

public class MetadataPropertyScheme {

    private final DTObject _data;

    MetadataPropertyScheme(String propertyName,
                           String monotypeTypeName, // 属性的类型，注意，如果是集合，则是集合的成员的类型
                           DomainPropertyCategory category,
                           boolean lazy) {
        _data = DTObject.editable();
        _data.setString("name", propertyName);
        _data.setByte("category", category.getValue());
        _data.setString("monotype", monotypeTypeName);
        _data.setBoolean("lazy", lazy);
    }

    /**
     * 为属性添加验证器
     *
     * @param validatorName
     * @param validatorData
     */
    public void addValidator(String validatorName,
                             DTObject validatorData) {
        var val = DTObject.editable();
        val.setString("name", validatorName);
        if (validatorData != null && !validatorData.isEmpty()) {
            val.combineObject("data", validatorData);
        }
        _data.push("vals", val);
    }

    DTObject getData() {
        return _data;
    }


}
