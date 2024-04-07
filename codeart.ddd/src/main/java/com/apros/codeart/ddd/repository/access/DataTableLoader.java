package com.apros.codeart.ddd.repository.access;

import java.util.HashMap;
import java.util.Map;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.metadata.DomainObjectCategory;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;

public final class DataTableLoader {

	private DataTableLoader() {

	}

	public static void load(Iterable<Class<? extends IDomainObject>> domainTypes) {

		// 先将起点表创建
		for (var domainType : domainTypes) {
			var objectMeta = ObjectMetaLoader.get(domainType);
			if (objectMeta.category() == DomainObjectCategory.AggregateRoot) {
				createRoot(domainType);
			}
		}

		// 再加载完整定义（这时候会根据对象关系创建中间表和建立各个表之间的连接）
		for (var domainType : domainTypes) {
			var table = getRoot(domainType);
			loadRoot(table);
		}
	}

	private static Map<Class<?>, DataTable> _roots = new HashMap<>();

	/**
	 * 
	 * 创建起点表（由领域类型映射的表）
	 * 
	 * @param domainType
	 * @return
	 */
	private static DataTable createRoot(Class<?> domainType) {
		var objectMeta = ObjectMetaLoader.get(domainType);
		var name = domainType.getSimpleName();

		var tableType = DataTableType.AggregateRoot;

		var id = name; // 起始表的编号就是表名
		var table = new DataTable(id, domainType, tableType, name);
		_roots.put(domainType, table);

		return table;
	}

	static DataTable getRoot(Class<?> objectType) {
		return _roots.get(objectType);
	}

	/**
	 * 加载起始表（也就是独立的内聚根对象对应的表）
	 * 
	 * @param table
	 */
	static void loadRoot(DataTable table) {
		var objectType = table.objectType();
		var objectMeta = ObjectMetaLoader.get(objectType);
		var mapper = DataMapperFactory.create(objectMeta);
		var objectFields = mapper.getObjectFields(objectMeta);

		table.setChain(ObjectChain.Empty);

		_tables.put(table.id(), table);

	}

//	#region 过滤字段，得到表的字段信息

	protected static Iterable<IDataField> GetTableFields(Iterable<IDataField> objectFields) {
		List<IDataField> fields = new List<IDataField>();
		for (var i = 0; i < objectFields.Count; i++) {
			var objectField = objectFields[i];
			FillFields(fields, objectField);
		}
		return fields;
	}

	private static bool FillFields(List<IDataField> fields, IDataField current)
	 {
	     string name = string.IsNullOrEmpty(current.Name) ? current.GetPropertyName() : current.Name;
	     switch (current.FieldType)
	     {
	         case DataFieldType.GeneratedField:
	             {
	                 fields.Add(current); //对于生成的键，直接追加
	                 return true;
	             }
	         case DataFieldType.Value:
	             {
	                 var valueField = current as ValueField;
	                 //存值
	                 var field = new ValueField(current.Tip, valueField.DbFieldTypes.ToArray())
	                 {
	                     Name = name,
	                     ParentMemberField = current.ParentMemberField
	                 };
	                 fields.Add(field);
	                 return true;
	             }
	         case DataFieldType.EntityObject:
	         case DataFieldType.EntityObjectPro:
	         case DataFieldType.AggregateRoot:
	             {
	                 //存外键即可
	                 var idAttr = DomainProperty.GetProperty(current.Tip.PropertyType, EntityObject.IdPropertyName).RepositoryTip;

	                 var field = new ValueField(idAttr)
	                 {
	                     Name = _getIdName(name),
	                     ParentMemberField = current
	                 };
	                 fields.Add(field);
	                 return true;
	             }
	         case DataFieldType.ValueObject:
	             {
	                 var primaryKey = GeneratedField.CreateValueObjectPrimaryKey(current.Tip.PropertyType);
	                 var field = new ValueField(primaryKey.Tip)
	                 {
	                     Name = _getIdName(name),
	                     ParentMemberField = current
	                 };
	                 fields.Add(field);
	                 return true;
	             }default:

	{
		current.Name = name; // 对于其他的类型，只是赋值字段名称
		break;
	}
	}return false;}

//	#endregion

	/// <summary>
	///
	/// </summary>
	/// <param name="memberField">表所属的成员字段</param>
	/// <param name="tableName"></param>
	/// <returns></returns>
//	private static string GetUniqueKey(IDataField memberField, string rootTableName, string tableName) {
//		if (memberField == null)
//			return tableName; // 这肯定是根
//		var chain = new ObjectChain(memberField);
//		// 由于不同类型的表名不同，所以没有加入对象名称作为计算结果
//		// 由于isSnapshot的值不同表名也不同，所以没有加入对象名称作为计算结果
//		return string.Format("{0}_{1}+{2}", rootTableName, chain.PathCode, tableName);
//	}

	private static void load(DataTable table,Iterable<IDataField> objectFields, DataTable root, DataTable master,IDataField memberField)
{
//补全memberField信息
if(memberField != null)
{
if (memberField.ParentMemberField == null)
memberField.ParentMemberField = master?.MemberField;

if (memberField.MasterTableName == null)
memberField.MasterTableName = master?.Name;

if (memberField.TableName == null)
memberField.TableName = tableName;

}

var table = GetBuildtimeIndex(memberField, root?.Name, tableName);
if (table != null) return table; //防止死循环
var copyFields = objectFields.ToList();
//获取字段信息
var tableFields = GetTableFields(copyFields); //得到表需要存储的字段集合
//得到表和子表信息
table = new DataTable(objectType,
            isSnapshot,
            root,
            master,
            tableType,
            tableName,
            tableFields,
            objectFields,
            memberField);
return table;
}

	private static Map<String, DataTable> _tables = new HashMap<>();

}