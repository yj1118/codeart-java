package apros.codeart.ddd.repository.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import apros.codeart.TestSupport;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.metadata.DomainObjectCategory;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.util.ListUtil;

/**
 * 
 */
final class DataTableLoader {

	private DataTableLoader() {

	}

	public static void load(Iterable<Class<? extends IDomainObject>> domainTypes) {
		for (var domainType : domainTypes) {
			var objectMeta = ObjectMetaLoader.get(domainType);
			if (objectMeta.category() == DomainObjectCategory.AggregateRoot) {
				createRoot(domainType);
			}
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

		String tableName = domainType.getSimpleName();

		var table = tryCreate(tableName, null, null, () -> {
			var objectFields = DataTableUtil.getObjectFields(domainType);
			return new DataTable(domainType, DataTableType.AggregateRoot, tableName, objectFields, null, null, null);
		});

		_roots.put(domainType, table);

		return table;
	}

	public static DataTable getRoot(Class<?> objectType) {
		return _roots.get(objectType);
	}

	/// <summary>
	/// 创建实体对象的表
	/// </summary>
	/// <param name="root"></param>
	/// <param name="master"></param>
	/// <param name="memberField"></param>
	/// <param name="isMultiple"></param>
	/// <returns></returns>
	public static DataTable createEntityObject(DataTable root, DataTable master, IDataField memberField,
			Class<?> objectType) {

		String tableName = objectType.getSimpleName();

		return tryCreate(tableName, root, memberField, () -> {
			// 注意，在内聚模型中，只要是实体对象，那么它就是相对于内聚根的实体对象，而不是所在对象的实体对象
			// 因此，所有的实体对象，外键存放的都是内聚根的编号
			var fields = new ArrayList<IDataField>();
			// 增加对象定义的领域属性
			ListUtil.addRange(fields, DataTableUtil.getObjectFields(objectType));
			fields.add(GeneratedField.createAssociatedCount(objectType));// 追加被引用次数

			return new DataTable(objectType, // 根据主表，判断是否为快照
					DataTableType.EntityObject, tableName, fields, root, master, memberField);
		});

	}

	/**
	 * 创建值对象的表
	 * 
	 * @param root
	 * @param master
	 * @param memberField
	 * @param objectType
	 * @return
	 */
	public static DataTable createValueObject(DataTable root, DataTable master, IDataField memberField,
			Class<?> objectType) {
		String tableName = objectType.getSimpleName();

		return tryCreate(tableName, root, memberField, () -> {
			var fields = new ArrayList<IDataField>();
			fields.add(GeneratedField.createValueObjectPrimaryKey(objectType)); // 追加主键
			// 增加对象定义的领域属性
			ListUtil.addRange(fields, DataTableUtil.getObjectFields(objectType));
			fields.add(GeneratedField.createAssociatedCount(objectType));// 追加被引用次数

			return new DataTable(objectType, // 根据主表，判断是否为快照
					DataTableType.ValueObject, tableName, fields, root, master, memberField);
		});
	}

	public static DataTable createEntityObjectList(DataTable root, DataTable master, IDataField memberField,
			Class<?> objectType) {
		// 需要创建EntityObject从表和中间表
		var slave = createEntityObject(root, master, memberField, objectType);
		var middle = createMiddleTable(slave, memberField);
		slave.setMiddle(middle);
		return slave;
	}

	/**
	 * 
	 * 类似List（int）这样的值成员的集合所对应的表
	 * 
	 * @param root
	 * @param master
	 * @param memberField
	 * @param objectType
	 * @return
	 */
	public static DataTable createValueList(DataTable root, DataTable master, IDataField memberField,
			Class<?> objectType) {
		String tableName = String.format("%s_%s", master.name(), memberField.name());

		return tryCreate(tableName, root, memberField, () -> {
			var valueListField = (ValueListField) memberField;

			Iterable<IDataField> fields = null;
			var reflectedType = valueListField.reflectedType(); // 就算集合的成员类型各自不同，但是他们肯定继承至同一个根类，因此中间表是统一一个类型的

			if (root.same(master)) {
				var rootField = DataTableUtil.getForeignKey(master, GeneratedFieldType.RootKey,
						DbFieldType.NonclusteredIndex);
				var indexField = GeneratedField.createOrderIndex(objectType, DbFieldType.NonclusteredIndex);
				var valueField = GeneratedField.createPrimitiveValue(reflectedType, valueListField);
				fields = List.of(rootField, indexField, valueField);
			} else {
				var rootField = DataTableUtil.getForeignKey(root, GeneratedFieldType.RootKey,
						DbFieldType.NonclusteredIndex); // 中间表中追加根字段，可以有效防止数据重叠
				var masterField = DataTableUtil.getForeignKey(master, GeneratedFieldType.MasterKey,
						DbFieldType.NonclusteredIndex);
				var indexField = GeneratedField.createOrderIndex(objectType, DbFieldType.NonclusteredIndex);
				var valueField = GeneratedField.createPrimitiveValue(reflectedType, valueListField);

				fields = List.of(rootField, masterField, indexField, valueField);
			}

			// memberField.GetPropertyType()就是集合类型，中间表的objectType是集合类型
			return new DataTable(memberField.propertyType(), DataTableType.Middle, tableName, fields, root, master,
					memberField);
		});
	}

	public static DataTable createValueObjectList(DataTable root, DataTable master, IDataField memberField,
			Class<?> objectType) {
		var slave = createValueObject(root, master, memberField, objectType);
		var middle = createMiddleTable(slave, memberField);
		slave.setMiddle(middle);
		return slave;
	}

	/**
	 * 
	 * 创建根内部引用外部根时映射的表
	 * 
	 * @param root
	 * @param master
	 * @param memberField
	 * @param objectType
	 * @return
	 */
	public static DataTable createAggregateRoot(DataTable root, DataTable master, IDataField memberField,
			Class<?> objectType) {
		var tableName = objectType.getSimpleName();

		return tryCreate(tableName, root, memberField, () -> {
			var objectFields = DataTableUtil.getObjectFields(objectType);
			return new DataTable(objectType, DataTableType.AggregateRoot, tableName, objectFields, root, master,
					memberField);
		});
	}

	public static DataTable createAggregateRootList(DataTable root, DataTable master, IDataField memberField,
			Class<?> objectType) {
		// 字段为根对象的集合，那么仅创建中间表
		var slave = createAggregateRoot(root, master, memberField, objectType);
		var middle = createMiddleTable(slave, memberField);
		slave.setMiddle(middle);
		return slave;
	}

	private static DataTable createMiddleTable(DataTable slave, IDataField memberField) {
		var root = slave.root();
		var master = slave.master();

		String tableName = String.format("%s_%s", master.name(), memberField.name());

		return tryCreate(tableName, root, memberField, () -> {

			Iterable<IDataField> fields = null;
			var reflectedType = memberField.reflectedType(); // 就算集合的成员类型各自不同，但是他们肯定继承至同一个根类，因此中间表是统一一个类型的

			if (root.same(master)) {
				var rootField = DataTableUtil.getForeignKey(master, GeneratedFieldType.RootKey,
						DbFieldType.NonclusteredIndex);
				var slaveField = DataTableUtil.getForeignKey(slave, GeneratedFieldType.SlaveKey,
						DbFieldType.NonclusteredIndex);
				slaveField.parentMemberField(memberField);

				var indexField = GeneratedField.createOrderIndex(reflectedType, DbFieldType.NonclusteredIndex);

				// 注意，大多数查询都是以rootField 为条件, indexField为排序，输出slaveField字段，
				// 所以slaveField的位置在最后
				fields = List.of(rootField, indexField, slaveField);
			} else {
				var rootField = DataTableUtil.getForeignKey(root, GeneratedFieldType.RootKey,
						DbFieldType.NonclusteredIndex); // 中间表中追加根字段，可以有效防止数据重叠
				var masterField = DataTableUtil.getForeignKey(master, GeneratedFieldType.MasterKey,
						DbFieldType.NonclusteredIndex);
				var slaveField = DataTableUtil.getForeignKey(slave, GeneratedFieldType.SlaveKey,
						DbFieldType.NonclusteredIndex);
				slaveField.parentMemberField(memberField);

				var indexField = GeneratedField.createOrderIndex(reflectedType, DbFieldType.NonclusteredIndex);

				// 注意，大多数查询都是以rootField, masterField为条件, indexField为排序，输出slaveField字段，
				// 所以slaveField的位置在最后
				fields = List.of(rootField, masterField, indexField, slaveField);
			}

			// memberField.GetPropertyType()就是集合类型，中间表的objectType是集合类型
			var objectType = memberField.propertyType();
			var middle = new DataTable(objectType, DataTableType.Middle, tableName, fields, root, master, memberField);

			middle.slave(slave);

			// 如果从表是根，那么需要记录从表和中间表的联系，当删除根对象时，会删除该中间表的数据
			RootIsSlaveIndex.tryAdd(slave);

			return middle;
		});
	}

	private static DataTable tryCreate(String tableName, DataTable root, IDataField memberField,
			Supplier<DataTable> creator) {

		String key = DataTableUtil.getId(memberField, root, tableName);
		var table = getBuildtimeIndex(key);
		if (table != null)
			return table; // 防止死循环

		table = creator.get(); // 先创建表

		addBuildtimeIndex(key, table); // 加入缓存后

		table.loadChilds(); // 再加载子表，防止死循环

		DataTableGenerator.generate(table);

		return table;
	}

	// #region 构建时索引，主要用于防止循环引用导致的死循环

	private static DataTable getBuildtimeIndex(String key) {
		return _buildtimeIndex.get(key);
	}

	private static void addBuildtimeIndex(String key, DataTable table) {
		_buildtimeIndex.put(key, table);
	}

	private static Map<String, DataTable> _buildtimeIndex = new HashMap<String, DataTable>();

	/**
	 * 加载工作完毕后，清理为了加载而造成的资源
	 */
	public static void disposeTemp() {
		_buildtimeIndex.clear();
		_buildtimeIndex = null;
	}

	public static DataTable get(Class<?> objectType) {
		return DataTableGenerator.getTable(objectType);
	}

	/**
	 * 删除表
	 */
	@TestSupport
	public static void drop() {
		DataTableGenerator.drop();
	}

	/**
	 * 清空数据，但是不删除表
	 */
	@TestSupport
	public static void clearUp() {
		DataTableGenerator.clearUp();
	}

//	/**
//	 * 创建所有表信息，这主要用于支持测试
//	 */
//	@TestSupport
//	public static void generate() {
//		DataTableGenerator.generate();
//	}

	// #endregion

}
