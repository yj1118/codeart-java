package apros.codeart.ddd;

import java.util.Objects;
import java.util.UUID;

import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.HashUtil;

@MergeDomain
@FrameworkDomain
public class ValueObject extends DomainObject implements IValueObject {
	public ValueObject() {
		this.onConstructed();
	}

	private UUID _persistentIdentity;

	public UUID getPersistentIdentity() {
		return _persistentIdentity;
	}

	public void setPersistentIdentity(UUID value) {
		// 如果没有编号，那么值对象需要追加编号，有编号则意味着值对象在数据库中已存在
		if (this._persistentIdentity == null)
			_persistentIdentity = value;
	}

	@Override
	protected void readOnlyCheckUp() {
		if (this.isConstructing())
			return; // 构造阶段不处理

		throw new DomainDrivenException(
				Language.strings("codeart.ddd", "ValueObjectReadOnly", this.getClass().getName()));
	}

	/// <summary>
	/// 要通过数据判断值对象相等
	/// </summary>
	/// <param name="obj"></param>
	/// <returns></returns>
	@Override
	public boolean equals(Object obj) {
		var objectType = this.getClass();
		var targetType = obj.getClass();
		if (targetType != this.getClass())
			return false;

		var target = TypeUtil.as(obj, ValueObject.class);

		// 对比所有领域属性
		var properties = DomainProperty.getProperties(objectType);
		for (var property : properties) {
			if (!Objects.deepEquals(this.getValue(property), target.getValue(property))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {

		return HashUtil.hash32((hasher) -> {
			var properties = DomainProperty.getProperties(this.getClass());
			for (var property : properties) {
				hasher.append(property.name());
				hasher.append(this.getValue(property));
			}
		});
	}

}
