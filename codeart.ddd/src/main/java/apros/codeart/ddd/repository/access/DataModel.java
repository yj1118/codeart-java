package apros.codeart.ddd.repository.access;

import java.util.function.Consumer;

import apros.codeart.TestSupport;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.repository.Page;

final class DataModel {

	public Class<?> objectType() {
		return _objectMeta.objectType();
	}

	private DataTable _root;

	/**
	 * 根表的信息
	 * 
	 * @return
	 */
	public DataTable root() {
		return _root;
	}

	private ObjectMeta _objectMeta;

	public ObjectMeta objectMeta() {
		return _objectMeta;
	}

	DataModel(ObjectMeta objectMeta, DataTable root) {
		_objectMeta = objectMeta;
		_root = root;
	}

	public void insert(DomainObject obj) {
		_root.insert(obj);
	}

	public void update(DomainObject obj) {
		_root.update(obj);
	}

	public void delete(DomainObject obj) {
		_root.delete(obj);
	}

	public <T extends IDomainObject> T querySingle(Object id, QueryLevel level) {
		return _root.querySingle(id, level);
	}

	public <T extends IDomainObject> T querySingle(String expression, Consumer<MapData> fillArg, QueryLevel level) {
		return _root.querySingle(expression, fillArg, level);
	}

	public <T extends IDomainObject> Iterable<T> query(String expression, Consumer<MapData> fillArg, QueryLevel level) {
		return _root.query(expression, fillArg, level);
	}

	public <T extends IDomainObject> Page<T> query(String expression, int pageIndex, int pageSize,
			Consumer<MapData> fillArg) {
		return _root.query(expression, pageIndex, pageSize, fillArg);
	}

	public int getCount(String expression, Consumer<MapData> fillArg, QueryLevel level) {
		return _root.getCount(expression, fillArg, level);
	}

//	/**
//	 * 
//	 * 该方法实际上是执行程序员写得自定义命令，exporession由外部识别并执行
//	 * 
//	 * @param expression
//	 * @param fillArg
//	 * @param level
//	 * @param fillItems
//	 */
//	public void execute(String expression, Consumer<MapData> fillArg, QueryLevel level,
//			Consumer<Map<String, Object>> fillItems) {
//		_root.execute(expression, fillArg, level, fillItems);
//	}

//	@TestSupport
//	public static void generate() {
//		DataTableLoader.generate();
//	}

	@TestSupport
	public static void drop() {
		DataTableLoader.drop();
	}

	@TestSupport
	public static void clearUp() {
		DataTableLoader.clearUp();
	}

//	/// <summary>
//	/// 找出当前应用程序可能涉及到的表信息
//	/// </summary>
//	private static IEnumerable<string> _indexs;
//
//	static DataModel() {
//		try {
//			DomainObject.Initialize();
//			_indexs = GetIndexs();
//		} catch (Exception ex) {
//			throw ex;
//		}
//	}
//
//	private static IEnumerable<string> GetIndexs()
//	 {
//	     DomainObject.CheckInitialized();
//
//	     List<string> tables = new List<string>();
//	     foreach (var objectType in DomainObject.TypeIndex)
//	     {
//	         if (DomainObject.IsEmpty(objectType)) continue;
//	         var mapper = DataMapperFactory.Create(objectType);
//	         var fileds = mapper.GetObjectFields(objectType, false);
//	         tables.Add(objectType.Name);
//	         tables.AddRange(DataTable.GetRelatedNames(objectType, fileds));
//	     }
//	     return tables.Distinct();
//	 }

}
