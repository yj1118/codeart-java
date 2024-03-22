package com.apros.codeart.ddd;

public class PropertyValidationError extends ValidationError {
	public string PropertyName
	{
	    get;
	    internal set;
	}

	PropertyValidationError() {
	}

	public override void Clear()
	{
	    base.Clear();
	    this.PropertyName = string.Empty;
	}
}
