package apros.codeart.ddd.toolkit.tree;

import apros.codeart.ddd.DDDConfig;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Page;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.ddd.repository.access.DataSource;
import apros.codeart.ddd.repository.access.DatabaseType;
import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccess;

import java.util.function.Function;

@SafeAccess
public abstract class TreeNodeRepository<T extends TreeNode<T>> extends SqlRepository<T> implements ITreeNodeRepository {

    public abstract Class<T> getRootType();

    private String insert_function_name;
    private String delete_function_name;
    private String move_function_name;
    private String parents_function_name;

    @Override
    public void init() {
        super.init();
        var tableName = this.getRootType().getSimpleName();
        insert_function_name = tableName + "_tree_insert";
        delete_function_name = tableName + "_tree_delete";
        move_function_name = tableName + "_tree_move";
        parents_function_name = tableName + "_tree_parents";

        createInsertFunction();
        createDeleteFunction();
        createMoveFunction();
        createParentsFunction();
    }

    //region insert

    private void createInsertFunction() {
        var sql = getInsertFunctionSql();
        DataContext.using((conn) -> {
            var access = conn.access();
            access.nativeExecute(sql);
        });
    }

    private String getInsertFunctionSql() {
        if (DataSource.getDatabaseType() == DatabaseType.PostgreSql) {
            return _getInsertSqlByPG.apply(this.getRootType().getSimpleName());
        }
        throw new IllegalStateException("only support PostgreSql database");
    }

    private final Function<String, String> _getInsertSqlByPG = LazyIndexer.init((String tableName) -> {

        String functionName = insert_function_name;
        StringBuilder sql = new StringBuilder();

        sql.append(String.format("CREATE OR REPLACE FUNCTION %s(parentId bigint, id bigint)\n", functionName));
        sql.append("RETURNS void AS $$\n");
        sql.append("DECLARE\n");
        sql.append("    rootId bigint;\n");
        sql.append("    parentRight integer;\n");
        sql.append("BEGIN\n");

        sql.append("    IF parentId <= 0 THEN\n");
        sql.append("        rootId := id;\n");  // 自己是根节点
        sql.append("        parentRight := 0;\n"); // 没有父节点，不需要偏移
        sql.append("    ELSE\n");

        sql.append(String.format("        SELECT \"RootId\" INTO rootId FROM %s WHERE \"Id\" = parentId;\n", tableName));
        sql.append(String.format("        SELECT \"Id\" FROM %s WHERE \"RootId\" = rootId FOR UPDATE;\n", tableName));
        sql.append(String.format("        SELECT \"Right\" INTO parentRight FROM %s WHERE \"Id\" = parentId;\n", tableName));

        sql.append("        IF parentRight IS NULL THEN\n");
        sql.append("            parentRight := 0;\n");
        sql.append("        ELSE\n");
        sql.append(String.format("            UPDATE %s SET \"Right\" = \"Right\" + 2 WHERE \"Right\" >= parentRight AND \"RootId\" = rootId;\n", tableName));
        sql.append(String.format("            UPDATE %s SET \"Left\" = \"Left\" + 2 WHERE \"Left\" >= parentRight AND  \"RootId\" = rootId;\n", tableName));
        sql.append("        END IF;\n");

        sql.append("    END IF;\n");

        sql.append(String.format("    UPDATE %s SET \"Left\" = parentRight, \"Right\" = parentRight + 1, \"RootId\" = rootId, \"Moving\" = false WHERE \"Id\" = id;\n", tableName));

        sql.append("END;\n");
        sql.append("$$ LANGUAGE plpgsql;\n");

        return sql.toString();

    });

    //endregion

    //region delete

    private void createDeleteFunction() {
        var sql = getDeleteFunctionSql();
        DataContext.using((conn) -> {
            var access = conn.access();
            access.nativeExecute(sql);
        });
    }

    private String getDeleteFunctionSql() {
        if (DataSource.getDatabaseType() == DatabaseType.PostgreSql) {
            return _getDeleteSqlByPG.apply(this.getRootType().getSimpleName());
        }
        throw new IllegalStateException("only support PostgreSql database");
    }

    private final Function<String, String> _getDeleteSqlByPG = LazyIndexer.init((String tableName) -> {

        String functionName = delete_function_name;

        StringBuilder sql = new StringBuilder();

        sql.append(String.format("CREATE OR REPLACE FUNCTION %s(id bigint)\n", functionName));
        sql.append("RETURNS void AS $$\n");
        sql.append("DECLARE\n");
        sql.append("    rootId bigint;\n");
        sql.append("    lft int;\n");
        sql.append("    rgt int;\n");
        sql.append("    disValue int;\n");
        sql.append("BEGIN\n");

        // 获取当前节点的 Left, Right, RootId
        sql.append(String.format("    SELECT \"Left\", \"Right\", \"RootId\" INTO lft, rgt, rootId FROM %s WHERE \"Id\" = id;\n", tableName));

        // 锁整棵树
        sql.append(String.format("    SELECT \"Id\" FROM %s WHERE \"RootId\" = rootId FOR UPDATE;\n", tableName));

        // 计算差值
        sql.append("    disValue := rgt - lft + 1;\n");

        // 更新右值
        sql.append(String.format("    UPDATE %s SET \"Right\" = \"Right\" - disValue WHERE \"Right\" >= rgt AND \"RootId\" = rootId;\n", tableName));

        // 更新左值
        sql.append(String.format("    UPDATE %s SET \"Left\" = \"Left\" - disValue WHERE \"Left\" >= rgt AND \"RootId\" = rootId;\n", tableName));

        sql.append("END;\n");
        sql.append("$$ LANGUAGE plpgsql;\n");

        return sql.toString();

    });

    //endregion

    //region move

    private void createMoveFunction() {
        var sql = getMoveFunctionSql();
        DataContext.using((conn) -> {
            var access = conn.access();
            access.nativeExecute(sql);
        });
    }

    private String getMoveFunctionSql() {
        if (DataSource.getDatabaseType() == DatabaseType.PostgreSql) {
            return _getMoveSqlByPG.apply(this.getRootType().getSimpleName());
        }
        throw new IllegalStateException("only support PostgreSql database");
    }

    private final Function<String, String> _getMoveSqlByPG = LazyIndexer.init((String tableName) -> {

        String functionName = move_function_name;

        StringBuilder sql = new StringBuilder();

        sql.append(String.format("CREATE OR REPLACE FUNCTION %s(currentId bigint, targetId bigint)\n", functionName));
        sql.append("RETURNS void AS $$\n");
        sql.append("DECLARE\n");
        sql.append("    rootId bigint;\n");
        sql.append("    lft int;\n");
        sql.append("    rgt int;\n");
        sql.append("    disValue int;\n");
        sql.append("    prgt int;\n");
        sql.append("    newLft int;\n");
        sql.append("    lftDisValue int;\n");
        sql.append("BEGIN\n");

        // 获取当前节点信息
        sql.append(String.format("    SELECT \"Left\", \"Right\", \"RootId\" INTO lft, rgt, rootId FROM %s WHERE \"Id\" = currentId;\n", tableName));

        // 锁整棵树
        sql.append(String.format("    SELECT \"Id\" FROM %s WHERE \"RootId\" = rootId FOR UPDATE;\n", tableName));

        // 计算差值
        sql.append("    disValue := rgt - lft + 1;\n");

        // 标记子树 moving=1
        sql.append(String.format("    UPDATE %s SET \"Moving\" = true WHERE \"Left\" > lft AND \"Left\" < rgt AND \"RootId\" = rootId;\n", tableName));

        // 删除当前位置（非子树）
        sql.append(String.format("    UPDATE %s SET \"Right\" = \"Right\" - disValue WHERE \"Right\" >= rgt AND \"RootId\" = rootId AND \"Moving\" = false;\n", tableName));
        sql.append(String.format("    UPDATE %s SET \"Left\" = \"Left\" - disValue WHERE \"Left\" >= rgt AND \"RootId\" = rootId AND \"Moving\" = false;\n", tableName));

        // 获取目标父节点的右值
        sql.append(String.format("    SELECT \"Right\" INTO prgt FROM %s WHERE \"Id\" = targetId;\n", tableName));
        sql.append("    IF prgt IS NULL THEN\n");
        sql.append("        prgt := 0;\n");
        sql.append("    ELSE\n");
        sql.append(String.format("        UPDATE %s SET \"Right\" = \"Right\" + disValue WHERE \"Right\" >= prgt AND \"RootId\" = rootId AND \"Moving\" = false;\n", tableName));
        sql.append(String.format("        UPDATE %s SET \"Left\" = \"Left\" + disValue WHERE \"Left\" >= prgt AND \"RootId\" = rootId AND \"Moving\" = false;\n", tableName));
        sql.append("    END IF;\n");

        // 更新自身位置
        sql.append("    newLft := prgt;\n");
        sql.append(String.format("    UPDATE %s SET \"Left\" = newLft, \"Right\" = newLft + disValue - 1, \"RootId\" = rootId WHERE \"Id\" = currentId;\n", tableName));

        // 更新子树位置
        sql.append("    lftDisValue := newLft - lft;\n");
        sql.append(String.format("    UPDATE %s SET \"Left\" = \"Left\" + lftDisValue, \"Right\" = \"Right\" + lftDisValue, \"Moving\" = false WHERE \"Left\" > lft AND \"Left\" < rgt AND \"RootId\" = rootId AND \"Moving\" = true;\n", tableName));

        sql.append("END;\n");
        sql.append("$$ LANGUAGE plpgsql;\n");

        return sql.toString();

    });

    //endregion

    //region parents

    private void createParentsFunction() {
        var sql = getParentsFunctionSql();
        DataContext.using((conn) -> {
            var access = conn.access();
            access.nativeExecute(sql);
        });
    }

    private String getParentsFunctionSql() {
        if (DataSource.getDatabaseType() == DatabaseType.PostgreSql) {
            return _getParentsSqlByPG.apply(this.getRootType().getSimpleName());
        }
        throw new IllegalStateException("only support PostgreSql database");
    }

    private final Function<String, String> _getParentsSqlByPG = LazyIndexer.init((String tableName) -> {

        String functionName = parents_function_name;
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("CREATE OR REPLACE FUNCTION %s(currentId bigint)\n", functionName));
        sb.append("RETURNS TABLE(Id bigint) AS $$\n");
        sb.append("DECLARE\n");
        sb.append("    lft int;\n");
        sb.append("    rgt int;\n");
        sb.append("    rootId bigint;\n");
        sb.append("BEGIN\n");

        // 获取当前节点的 lft、rgt、RootId
        sb.append(String.format("    SELECT \"Left\", \"Right\", \"RootId\" INTO lft, rgt, rootId FROM %s WHERE \"Id\" = currentId;\n", tableName));

        // 查询所有祖先节点
        sb.append(String.format("    RETURN QUERY SELECT \"Id\" FROM %s WHERE \"Left\" < lft AND \"Right\" > rgt AND \"RootId\" = rootId ORDER BY \"Left\";\n", tableName));

        sb.append("END;\n");
        sb.append("$$ LANGUAGE plpgsql;\n");

        return sb.toString();

    });

    //endregion


    @Override
    public void onAdded(T obj) {
        super.onAdded(obj);

        var parentId = obj.parent().getIdentity();
        var id = obj.getIdentity();

        DataPortal.direct((conn) ->
        {
            conn.access().execute(String.format("%s(%s,%s)", insert_function_name, parentId, id));
        });
    }

    @Override
    public void onDeleted(T obj) {
        super.onDeleted(obj);

        var id = obj.getIdentity();

        DataPortal.direct((conn) ->
        {
            conn.access().execute(String.format("%s(%s)", delete_function_name, id));
        });
    }


    @Override
    public void move(TreeNode<?> current, TreeNode<?> target) {
        var currentId = current.getIdentity();
        var targetId = target.getIdentity();
        DataPortal.direct((conn) ->
        {
            conn.access().execute(String.format("%s(%s,%s)", move_function_name, currentId, targetId));
        });
    }
}