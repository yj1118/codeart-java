package com.apros.codeart.ddd;

public interface IDomainCollection {
	DomainObject getParent();

	void setParent(DomainObject parent);
}
