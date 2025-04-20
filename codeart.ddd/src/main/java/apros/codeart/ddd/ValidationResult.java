package apros.codeart.ddd;

import java.util.ArrayList;

import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public class ValidationResult {

    private ArrayList<ValidationError> _errors;

    private void try_init_errors() {
        if (_errors == null)
            _errors = new ArrayList<ValidationError>();
    }

    private ValidationResult() {

    }

    public int getErrorCount() {
        return _errors == null ? 0 : _errors.size();
    }

    public boolean isSatisfied() {
        return _errors == null || _errors.isEmpty();
    }

    public void append(DomainProperty property, String code, String message) {
        var error = createPropertyError(property.name(), code, message);
        append(error);
    }

    /// <summary>
    /// 向验证结果中添加一个错误
    /// </summary>
    public void append(String propertyName, String errorCode, String message) {
        var error = createPropertyError(propertyName, errorCode, message);
        append(error);
    }

    public void append(String message) {
        var error = createError(StringUtil.empty(), message);
        append(error);
    }

    public void append(String errorCode, String message) {
        var error = createError(errorCode, message);
        append(error);
    }

    private void append(ValidationError error) {
        try_init_errors();
        _errors.add(error);
    }

    public String getMessage() {
        if (this.isSatisfied())
            return "success";
        else {
            return StringUtil.lines(_errors, ValidationError::getMessage);
        }
    }

    public void combine(ValidationResult result) {
        if (result.getErrorCount() == 0)
            return;
        try_init_errors();
        _errors.addAll(result._errors);
    }

    /**
     * 检测验证结果是否包含指定的错误代码
     *
     * @param errorCode
     * @return
     */
    public boolean containsCode(String errorCode) {
        return this.getError(errorCode) != null;
    }

    public ValidationError getError(String errorCode) {
        return ListUtil.find(_errors, (item) -> {
            return item.getCode().equalsIgnoreCase(errorCode);
        });
    }

    public void remove(String errorCode) {
        if (this.getErrorCount() == 0)
            return;
        var e = this.getError(errorCode);
        if (e != null)
            _errors.remove(e);
    }

    void clear() {
        if (this.getErrorCount() > 0)
            _errors.clear();
    }

    public static final ValidationResult Satisfied = new ValidationResult();

    /**
     * 获得一个验证结果对象，该对象会与数据上下文共享生命周期
     *
     * @return
     */
    public static ValidationResult create() {
        return new ValidationResult();
    }

    private static PropertyValidationError createPropertyError(String propertyName, String errorCode, String message) {
        return new PropertyValidationError(message, message, propertyName);
    }

    private static ValidationError createError(String errorCode, String message) {
        return new ValidationError(errorCode, message);
    }

}
