package com.apros.codeart.ddd.repository.access;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.apros.codeart.TestSupport;
import com.apros.codeart.ddd.repository.DataContext;
import com.apros.codeart.ddd.repository.access.query.CreateTable;
import com.apros.codeart.util.ListUtil;

/**
 * 该类负责在数据库里生成表
 */
final class DataTableGenerator {

	private DataTableGenerator() {

	}

	private void generate(DataTable table) {
		ifUnBuilt(table.name(), () -> {
			// 开启独立事务，这样创建表的操作就和后续的增删改查没有冲突了，不会造成死锁
			DataContext.newScope((access) -> {
				var agent = DataSource.getAgent();
				var builder = agent.getQueryBuilder(CreateTable.class);
				var sql = builder.build(new QueryDescription(table));
				access.execute(sql);
			});
			return table;
		});
	}

	private static ArrayList<DataTable> _generated = new ArrayList<>();

	private static void ifUnBuilt(String name, Supplier<DataTable> action) {
		if (ListUtil.contains(_generated, (t) -> t.name().equals(name)))
			return;
		synchronized (_generated) {
			if (ListUtil.contains(_generated, (t) -> t.name().equals(name)))
				return;
			var table = action.get();
			_generated.add(table);
		}
	}

	/// <summary>
	/// 创建所有表信息，这主要用于支持测试
	/// </summary>
	@TestSupport
	static void generate() {
		// 开启独立事务，这样创建表的操作就和后续的增删改查没有冲突了，不会造成死锁
		DataContext.newScope(() -> {
			for (var table : _generated) {
				var sql = CreateTable.Create(index).Build(null, index);
				SqlHelper.Execute(sql);
			}
		});
	}

	/// <summary>
	/// 删除表
	/// </summary>
	@TestSupport
	static void drop() {
		DataContext.newScope(() -> {
			for (var table : _generated) {
				var sql = DropTable.Create(tableName).Build(null, null);
				SqlHelper.Execute(tableName, sql);
			}
		});
	}

	/// <summary>
	/// 清空数据
	/// </summary>
	@TestSupport
	static void clearUp()
	 {
	     DataContext.newScope(() ->
	     {
	    	 for (var table : _generated) {
	         {
	             var sql = ClearTable.Create(tableName).Build(null, null);
	             SqlHelper.Execute(tableName, sql);
	         }
	     });
	 }

	/**
	 * 
	 * 得到已在数据库中创建的表
	 * 
	 * @param objectType
	 * @return
	 */
	public static DataTable getTable(Class<?> objectType) {
		for (var table : _generated) {
			if (table.objectType().equals(objectType))
				return table;
		}
		return null;
	}

}
