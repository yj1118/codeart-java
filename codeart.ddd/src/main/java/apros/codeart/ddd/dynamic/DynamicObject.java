package apros.codeart.ddd.dynamic;

import java.util.ArrayList;

import apros.codeart.ddd.DDDConfig;
import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.FrameworkDomain;
import apros.codeart.ddd.MergeDomain;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.PrimitiveUtil;

@MergeDomain
@FrameworkDomain
public class DynamicObject extends DomainObject implements IDynamicObject {

	private boolean _isEmpty;

	public boolean isEmpty() {
		return _isEmpty;
	}

	public DynamicObject(boolean isEmpty) {
		_isEmpty = isEmpty;
		this.onConstructed();
	}

	@ConstructorRepository
	public DynamicObject() {
		_isEmpty = false;
		this.onConstructed();
	}

	private Iterable<DomainProperty> _properties;

	public Iterable<DomainProperty> properties() {
		if (_properties == null) {
			var meta = ObjectMetaLoader.get(this.getClass());
			_properties = ListUtil.map(meta.properties(), (propertyMeta) -> {
				return DomainProperty.getProperty(this.getClass(), propertyMeta.name());
			});
		}
		return _properties;
	}

	/**
	 * 从dto中加载数据
	 */
	public void load(DTObject data) {
		for (var property : this.properties()) {
			var value = data.getValue(property.name(), false);
			if (value == null)
				continue;

			var obj = TypeUtil.as(value, DTObject.class);
			if (obj != null) {
				this.setValue(property, getObjectValue(property, obj));
				continue;
			}

			var objs = TypeUtil.as(value, Iterable.class);
			if (objs != null) {
				this.setValue(property, getListValue(property, objs));
				continue;
			}
			this.setValue(property, getPrimitiveValue(property, value));
		}
	}

	@SuppressWarnings("unchecked")
	public DTObject getData() {
		var data = DTObject.editable();
		for (var property : this.properties()) {
			var value = this.getValue(property);
			var obj = TypeUtil.as(value, DynamicObject.class);
			if (obj != null) {
				value = obj.getData(); // 对象
				data.setValue(property.name(), value);
				continue;
			}

			var list = TypeUtil.as(value, Iterable.class);
			if (list != null) {
				// 集合
				data.push(property.name(), list, (item) -> {
					var o = TypeUtil.as(item, DynamicObject.class);
					if (o != null)
						return o.getData();

					return DTObject.value(item);
				});
				continue;
			}

			data.setValue(property.name(), value); // 值
		}
		return data;
	}

	private Object getObjectValue(DomainProperty property, DTObject value) {
		switch (property.category()) {
		case DomainPropertyCategory.AggregateRoot:
		case DomainPropertyCategory.EntityObject:
		case DomainPropertyCategory.ValueObject: {

			var objType = property.monotype();
			return createInstance(objType, value);
		}
		default:
			throw new DomainDrivenException(Language.strings("DynamicObjectLoadError", this.getClass().getName()));
		}

	}

	private Object getListValue(DomainProperty property, Iterable<DTObject> values) {
		var list = new DynamicCollection(property);
		list.setParent(this);

		switch (property.category()) {
		case DomainPropertyCategory.AggregateRootList:
		case DomainPropertyCategory.EntityObjectList:
		case DomainPropertyCategory.ValueObjectList: {
			var elementType = property.monotype();
			for (DTObject value : values) {
				var obj = createInstance(elementType, value);
				list.add(obj);
			}
			return list;
		}
		case DomainPropertyCategory.PrimitiveList: {
			for (DTObject value : values) {
				if (!value.isSingleValue())
					throw new DomainDrivenException(
							Language.strings("DynamicObjectLoadError", this.getClass().getName()));
				list.add(value.getValue());
			}
			return list;
		}
		default:
			throw new DomainDrivenException(Language.strings("DynamicObjectLoadError", this.getClass().getName()));
		}

	}

	private Object getPrimitiveValue(DomainProperty property, Object value) {
		if (property.category() == DomainPropertyCategory.Primitive) {
			return PrimitiveUtil.convert(value, property.monotype());
		}
		throw new DomainDrivenException(Language.strings("DynamicObjectLoadError", this.getClass().getName()));
	}

	/// <summary>
	/// 以<paramref name="data"/>为数据格式创建前定义的类型的实例
	/// </summary>
	/// <param name="data"></param>
	static DomainObject createInstance(Class<?> objectType, DTObject data) {
		if (!objectType.isAssignableFrom(IDynamicObject.class)) {
			throw new DomainDrivenException(Language.strings("NotSupportRefNative"));
		}

		if (data.isEmpty())
			return DomainObject.getEmpty(objectType);
		DynamicObject obj = (DynamicObject) Activator.createInstance(objectType);
		// 加载数据
		obj.load(data);
		return obj;
	}

	/**
	 * 将对象 {@target} 的数据同步到当前对象中
	 * 
	 * @param target
	 */
	public void sync(DynamicObject target) {
		for (var property : this.properties()) {
			var value = target.getValue(property);
			this.setValue(property, value);
		}
	}

	/**
	 * 得到动态对象内部所有的动态根类型的成员对象（不包括当前对象自身，也不包括空的根对象）
	 */
	public Iterable<DynamicRoot> getRefRoots() {
		ArrayList<DynamicRoot> roots = new ArrayList<>();
		ArrayList<Object> processed = new ArrayList<>(); // 防止死循环的额外参数

		this.fillRefRoots(roots, processed, DynamicRoot.class);

		return roots;
	}

	/**
	 * 为了防止死循环，增加了该方法
	 * 
	 * @param roots 将当前对象直接或间接引用到的根对象，填充到 roots里
	 */
	protected <T extends DynamicRoot> void fillRefRoots(ArrayList<T> roots, ArrayList<Object> processed,
			Class<T> rootType) {
		for (var property : this.properties()) {
			switch (property.category()) {
			case DomainPropertyCategory.AggregateRoot: {
				var value = this.getValue(property);
				if (processed.contains(value))
					continue;
				processed.add(value);

				var root = TypeUtil.as(value, rootType);
				if (root != null && !root.isEmpty()) {
					roots.add(root);
					root.fillRefRoots(roots, processed, rootType);
				}
			}
				break;
			case DomainPropertyCategory.EntityObject:
			case DomainPropertyCategory.ValueObject: {
				var value = this.getValue(property);
				if (processed.contains(value))
					continue;
				processed.add(value);

				var obj = TypeUtil.as(value, DynamicObject.class);
				if (obj != null && !obj.isEmpty()) {
					obj.fillRefRoots(roots, processed, rootType);
				}
			}
				break;
			case DomainPropertyCategory.AggregateRootList: {
				var value = this.getValue(property);
				if (processed.contains(value))
					continue;
				processed.add(value);

				var list = TypeUtil.as(value, Iterable.class);
				for (var obj : list) {
					if (processed.contains(obj))
						continue;
					processed.add(obj);

					var root = TypeUtil.as(obj, rootType);
					if (root != null && !root.isEmpty()) {
						roots.add(root);
						root.fillRefRoots(roots, processed, rootType);
					}
				}
			}
				break;
			case DomainPropertyCategory.EntityObjectList:
			case DomainPropertyCategory.ValueObjectList: {
				var value = this.getValue(property);
				if (processed.contains(value))
					continue;
				processed.add(value);

				var list = TypeUtil.as(value, Iterable.class);
				for (var obj : list) {
					if (processed.contains(obj))
						continue;
					processed.add(obj);

					var o = TypeUtil.as(obj, DynamicObject.class);
					if (o != null && !o.isEmpty()) {
						o.fillRefRoots(roots, processed, rootType);
					}
				}

			}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 
	 * 获取通过配置文件得到的对象元数据
	 * 
	 * @param objectName
	 * @return
	 */
	public static DTObject getMetadata(String objectName) {
		var meta = DDDConfig.objectMeta();
		return meta.getObject(objectName, null);
	}

}
