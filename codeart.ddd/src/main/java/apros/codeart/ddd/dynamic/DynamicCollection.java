package apros.codeart.ddd.dynamic;

import apros.codeart.ddd.DomainCollection;
import apros.codeart.ddd.DomainProperty;

class DynamicCollection extends DomainCollection<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2374369813830873290L;

	public DynamicCollection(DomainProperty propertyInParent) {
		super(Object.class, propertyInParent);
	}

}
