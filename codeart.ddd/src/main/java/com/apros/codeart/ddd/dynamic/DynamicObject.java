package com.apros.codeart.ddd.dynamic;

import java.util.ArrayList;

import com.apros.codeart.ddd.DomainCollection;
import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.FrameworkDomain;
import com.apros.codeart.ddd.MergeDomain;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.ddd.metadata.DomainPropertyCategory;
import com.apros.codeart.ddd.repository.ConstructorRepository;
import com.apros.codeart.dto.DTObject;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.Activator;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.PrimitiveUtil;

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
		}
		throw new DomainDrivenException(Language.strings("DynamicObjectLoadError", this.getClass().getName()));
	}

	private Object getListValue(DomainProperty property, Iterable values) {
		var list = new DomainCollection(property.monotype(), property);
		list.setParent(this);

		switch (property.category()) {
		case DomainPropertyCategory.AggregateRootList:
		case DomainPropertyCategory.EntityObjectList:
		case DomainPropertyCategory.ValueObjectList: {
			var elementType = property.monotype();
			for (Object temp : values) {
				var value = (DTObject) temp;
				var obj = createInstance(elementType, value);
				list.add(obj);
			}
			return list;
		}
		case DomainPropertyCategory.PrimitiveList: {
			for (Object temp : values) {
				var value = (DTObject) temp;
				if (!value.isSingleValue())
					throw new DomainDrivenException(
							Language.strings("DynamicObjectLoadError", this.getClass().getName()));
				list.add(value.getValue());
			}
			return list;
		}
		}
		throw new DomainDrivenException(Language.strings("DynamicObjectLoadError", this.getClass().getName()));
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
	static DomainObject createInstance(Class<?> objectType, DTObject data)
	{
		if(!objectType.isAssignableFrom(IDynamicObject.class)) {
			throw new DomainDrivenException(Language.strings("NotSupportRefNative"); 
		}
		
	    if (data.isEmpty()) return DomainObject.getEmpty(objectType);
	    DynamicObject obj = (DynamicObject)Activator.createInstance(objectType);
	    //加载数据
	    obj.load(data);
	    return obj;
	}

	/**
	 * 将对象 {@target} 的数据同步到当前对象中
	 * 
	 * @param target
	 */
	void sync(DynamicObject target) {
		for (var property : this.properties()) {
			var value = target.getValue(property);
			this.setValue(property, value);
		}
	}

	/**
	 * 从dto中加载数据
	 */
	public Iterable<DynamicRoot> getRoots()
	 {
	     ArrayList<DynamicRoot> roots = new ArrayList<>();

	     for (var property : this.properties())
	     {
	         switch (property.category())
	         {
	             case DomainPropertyCategory.AggregateRoot:
	                 {
	                     var value = this.getValue(property);
	                     var root = (DynamicRoot)value;
	                     if(!root.isEmpty())
	                     {
	                         roots.add(root);
	                         ListUtil.addRange(roots, root.getRoots());
	                     }
	                 }
	                 break;
	             case DomainPropertyCategory.EntityObject:
	             case DomainPropertyCategory.ValueObject:
	                 {
	                     var value = this.getValue(property);
	                     var obj = (DynamicObject)value;
	                     if (!obj.isEmpty())
	                     {
	                    	 ListUtil.addRange(roots, obj.getRoots());
	                     }
	                 }
	                 break;
	             case DomainPropertyCategory.AggregateRootList:
	                 {
	                     var list = (Iterable)this.getValue(property);
	                     for (DynamicRoot root : list)
	                     {
	                         if (!root.isEmpty())
	                         {
	                             roots.add(root);
	                             ListUtil.addRange(roots, root.getRoots());
	                         }
	                     }
	                 }
	                 break;
	             case DomainPropertyCategory.EntityObjectList:
	             case DomainPropertyCategory.ValueObjectList:
	                 {
	                     var list = (Iterable)this.GetValue(property);
	                     for(DynamicObject obj : list)
	                     {
	                         if (!obj.isEmpty())
	                         {
	                        	 ListUtil.addRange(roots, obj.getRoots());
	                         }
	                     }
	                 }
	                 break;
	         }
	     }
	     return roots;
	 }

	#endregion

}
