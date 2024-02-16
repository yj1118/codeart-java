package apros.codeart.ddd;

import java.util.ArrayList;

import apros.codeart.pooling.PoolingException;
import apros.codeart.pooling.util.StringPool;

public class ValidationResult {

	private ArrayList<ValidationError> _errors;

	private ValidationResult() {
		_errors = new ArrayList<ValidationError>();
	}

	public int getErrorCount() {
		return _errors.size();
	}

	public boolean isSatisfied() {
		return _errors.size() == 0;
	}

	public void AddError(IDomainProperty property, String code, String message) {
		var error = CreatePropertyError(property.getName(), code, message);
		_errors.Add(error);
	}

	/// <summary>
	/// 向验证结果中添加一个错误
	/// </summary>
	public void AddError(string propertyName, string errorCode, string message) {
		var error = CreatePropertyError(propertyName, errorCode, message);
		_errors.Add(error);
	}

	public void AddError(string message) {
		var error = CreateError(string.Empty, message);
		_errors.Add(error);
	}

	public void AddError(string errorCode, string message) {
		var error = CreateError(errorCode, message);
		_errors.Add(error);
	}

	public String getMessage() throws PoolingException {
		if (this.isSatisfied())
			return "success";
		else {
			return StringPool.lines(_errors, (error) -> error.getMessage());
		}
	}

}
