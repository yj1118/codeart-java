package apros.codeart.ddd.repository.access;

import java.util.ArrayList;
import java.util.function.Supplier;

import apros.codeart.TestSupport;
import apros.codeart.ddd.DDDConfig;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.util.ListUtil;

/**
 * 该类负责在数据库里生成表
 */
final class DataTableGenerator {

    private DataTableGenerator() {

    }

    private static void createTable(DataAccess access, DataTable table) {
        var builder = DataSource.getQueryBuilder(CreateTableQB.class);
        var sql = builder.build(new QueryDescription(table));
        access.execute(sql);
    }

    public static void generate(DataTable table) {
        ifUnBuilt(table.name(), () -> {
            DataContext.using((conn) -> {
                var access = conn.access();
                if (DDDConfig.enableReset()) {
                    // 开启了重置，所以在创建表之前，把表删除，重新创建
                    dropTable(access, table);
                    DataPortal.dropIdentity(access, table);
                }
                createTable(access, table);
            });
            return table;
        });
    }

    private static final ArrayList<DataTable> _generated = new ArrayList<>();

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

//	/// <summary>
//	/// 创建所有表信息，这主要用于支持测试
//	/// </summary>
//	@TestSupport
//	static void generate() {
//		// 开启独立事务，这样创建表的操作就和后续的增删改查没有冲突了，不会造成死锁
//		DataContext.newScope((access) -> {
//			for (var table : _generated) {
//				createTable(access, table);
//			}
//		});
//	}

    private static void dropTable(DataAccess access, DataTable table) {
        var builder = DataSource.getQueryBuilder(DropTableQB.class);
        var sql = builder.build(new QueryDescription(table));
        access.execute(sql);
    }

    /// <summary>
    /// 删除表
    /// </summary>
    @TestSupport
    static void drop() {
        DataContext.newScope((conn) -> {
            var access = conn.access();
            for (var table : _generated) {
                dropTable(access, table);
            }
        });
    }

    /// <summary>
    /// 清空数据
    /// </summary>
    @TestSupport
    static void clearUp() {
        DataContext.newScope((conn) -> {
            var access = conn.access();
            for (var table : _generated) {
                var builder = DataSource.getQueryBuilder(ClearTableQB.class);
                var sql = builder.build(new QueryDescription(table));
                access.execute(sql);
            }
        });
    }

    /**
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
