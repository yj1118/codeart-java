package apros.codeart.ddd;

import apros.codeart.runtime.TypeUtil;

@MergeDomain
@FrameworkDomain
public abstract class EntityObject extends DomainObject implements IEntityObject {

	/**
	 * 统一领域对象中的标识符名称，这样在ORM处理等操作中会比较方便
	 */
	public static final String IdPropertyName = "Id";

	public abstract Object getIdentity();

	protected EntityObject() {
		this.onConstructed();
	}

	@Override
	public boolean equals(Object obj) {
		var target = TypeUtil.as(obj, EntityObject.class);
		if (target == null)
			return false;
		return this.getIdentity().equals(target.getIdentity());
	}

	@Override
	public int hashCode() {
		var key = this.getIdentity();
		if (key != null)
			return key.hashCode();
		return 0;
	}
}
