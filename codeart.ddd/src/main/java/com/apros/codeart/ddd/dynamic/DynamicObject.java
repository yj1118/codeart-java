package com.apros.codeart.ddd.dynamic;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.FrameworkDomain;
import com.apros.codeart.ddd.MergeDomain;
import com.apros.codeart.dto.DTObject;

@MergeDomain
@FrameworkDomain
public class DynamicObject extends DomainObject implements IDynamicObject {

	@ConstructorRepository
	public DynamicObject() {
		this.onConstructed();
	}

	/// <summary>
	/// 从dto中加载数据
	/// </summary>
	/// <param name="data"></param>
	public void load(DTObject data)
	 {
	     var properties = this.Define.Properties;
	     foreach (var property in properties)
	     {
	         var value = data.GetValue(property.Name, false);
	         if (value == null) continue;

	         var obj = value as DTObject;
	         if (obj != null)
	         {
	             this.SetValue(property, GetObjectValue(property, obj));
	             continue;
	         }

	         var objs = value as DTObjects;
	         if (objs != null)
	         {
	             this.SetValue(property, GetListValue(property, objs));
	             continue;
	         }
	         this.SetValue(property, GetPrimitiveValue(property, value));
	     }
	 }

	public DTObject GetData()
	 {
	     var data = DTObject.Create();
	     var properties = this.Define.Properties;
	     foreach (var property in properties)
	     {
	         var value = this.GetValue(property);
	         var obj = value as DynamicObject;
	         if (obj != null)
	         {
	             value = obj.GetData();  //对象
	             data.SetValue(property.Name, value);
	             continue;
	         }

	         var list = value as IEnumerable<DynamicObject>;
	         if (list != null)
	         {
	             //集合
	             data.Push(property.Name, list, (item) =>
	              {
	                  return item.GetData();
	              });
	             continue;
	         }

	         data.SetValue(property.Name, value); //值
	     }
	     return data;
	 }

	private object GetObjectValue(DomainProperty property, DTObject value)
	 {
	     switch (property.DomainPropertyType)
	     {
	         case DomainPropertyType.AggregateRoot:
	         case DomainPropertyType.EntityObject:
	         case DomainPropertyType.ValueObject:
	             {
	                 var objType = property.PropertyType as RuntimeObjectType;
	                 if (objType == null) throw new DomainDrivenException(string.Format(Strings.DynamicObjectLoadError, this.Define.TypeName));
	                 var objDefine = objType.Define;
	                 DynamicObject obj = objDefine.CreateInstance(value);
	                 return obj;
	             }
	     }
	     throw new DomainDrivenException(string.Format(Strings.DynamicObjectLoadError, this.Define.TypeName));
	 }

	private object GetListValue(DomainProperty property, DTObjects values)
	 {
	     IList list = property.PropertyType.CreateInstance() as IList;
	     if (list == null) throw new DomainDrivenException(string.Format(Strings.DynamicObjectLoadError, this.Define.TypeName));

	     switch (property.DomainPropertyType)
	     {
	         case DomainPropertyType.AggregateRootList:
	         case DomainPropertyType.EntityObjectList:
	         case DomainPropertyType.ValueObjectList:
	             {
	                 var elementType = property.DynamicType as RuntimeObjectType;
	                 if (elementType == null) throw new DomainDrivenException(string.Format(Strings.DynamicObjectLoadError, this.Define.TypeName));
	                 var objDefine = elementType.Define;
	                 foreach (DTObject value in values)
	                 {
	                     DynamicObject obj = objDefine.CreateInstance(value);
	                     list.Add(obj);
	                 }
	                 return list;
	             }
	         case DomainPropertyType.PrimitiveList:
	             {
	                 foreach (DTObject value in values)
	                 {
	                     if (!value.IsSingleValue)
	                         throw new DomainDrivenException(string.Format(Strings.DynamicObjectLoadError, this.Define.TypeName));
	                     list.Add(value.GetValue());
	                 }
	                 return list;
	             }
	     }
	     throw new DomainDrivenException(string.Format(Strings.DynamicObjectLoadError, this.Define.TypeName));
	 }

	private object GetPrimitiveValue(DomainProperty property, object value) {
		if (property.DomainPropertyType == DomainPropertyType.Primitive) {
			return DataUtil.ToValue(value, property.PropertyType);
		}
		throw new DomainDrivenException(string.Format(Strings.DynamicObjectLoadError, this.Define.TypeName));
	}

	#endregion

	#

	region 同步对象

	/// <summary>
	/// 将对象<paramref name="target"/>的数据同步到当前对象中
	/// </summary>
	/// <param name="target"></param>
	internal

	void Sync(DynamicObject target)
	 {
	     var properties = this.Define.Properties;
	     foreach (var property in properties)
	     {
	         var value = target.GetValue(property);
	         this.SetValue(property, value);
	     }
	 }

	/// <summary>
	/// 标记为快照
	/// </summary>
	internal

	void MarkSnapshot()
	 {
	     this.SetValue(this.Define.SnapshotProperty, true);
	 }

	#endregion

	#

	region 获取成员根对象

	/// <summary>
	/// 从dto中加载数据
	/// </summary>
	/// <param name="data"></param>
	public IEnumerable<DynamicRoot> GetRoots()
	 {
	     List<DynamicRoot> roots = new List<DynamicRoot>();
	     var properties = this.Define.Properties;
	     foreach (var property in properties)
	     {
	         switch (property.DomainPropertyType)
	         {
	             case DomainPropertyType.AggregateRoot:
	                 {
	                     var value = this.GetValue(property);
	                     var root = (DynamicRoot)value;
	                     if(!root.IsEmpty())
	                     {
	                         roots.Add(root);
	                         roots.AddRange(root.GetRoots());
	                     }
	                 }
	                 break;
	             case DomainPropertyType.EntityObject:
	             case DomainPropertyType.ValueObject:
	                 {
	                     var value = this.GetValue(property);
	                     var obj = (DynamicObject)value;
	                     if (!obj.IsEmpty())
	                     {
	                         roots.AddRange(obj.GetRoots());
	                     }
	                 }
	                 break;
	             case DomainPropertyType.AggregateRootList:
	                 {
	                     var list = (IEnumerable)this.GetValue(property);
	                     foreach (DynamicRoot root in list)
	                     {
	                         if (!root.IsEmpty())
	                         {
	                             roots.Add(root);
	                             roots.AddRange(root.GetRoots());
	                         }
	                     }
	                 }
	                 break;
	             case DomainPropertyType.EntityObjectList:
	             case DomainPropertyType.ValueObjectList:
	                 {
	                     var list = (IEnumerable)this.GetValue(property);
	                     foreach(DynamicObject obj in list)
	                     {
	                         if (!obj.IsEmpty())
	                         {
	                             roots.AddRange(obj.GetRoots());
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
