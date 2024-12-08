package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.*;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

/**
 * 基于表达式的查询,可以指定对象属性等表达式
 */
public final class ExpressionHelper {

    private ExpressionHelper() {
    }

    public static String getObjectSql(DataTable target, QueryLevel level, SqlDefinition definition, ILockSql lockSql) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        if (level == QueryLevel.NONE) {
            sql.append(getDISTINCT(definition));
        }

        StringUtil.appendLine(sql, getSelectFieldsSql(target, definition));
        StringUtil.appendLine(sql, " FROM ");
        StringUtil.append(sql, getTableSql(target, level, definition, lockSql));

        return getFinallyObjectSql(sql.toString(), target, definition, level);
    }

    private static String getDISTINCT(SqlDefinition definition) {
        // 因为我们提供的是对象表达式，所以对象的查询是要去重复的
        // 对象可能引用了集合属性，导致对象在查询时重复，在这里就处理，外界可以放心查询
        // 如果遇到必须多行不重复的情况，就自己写sql实现，因为这种查询一般都不是基于对象的查询
        if (DataSource.getDatabaseType() == DatabaseType.PostgreSql) {
            String distinctFields = null;
            String idField = EntityObject.IdPropertyName;
            if (definition.order().isEmpty()) {
                distinctFields = idField;
            } else {

                var columns = ListUtil.map(definition.columns().order(), (o) -> {
                    return o; //可能要调整具体蒜贩
                });

                var findId = false;
                for (var column : columns) {
                    if (column.equalsIgnoreCase(idField)) {
                        findId = true;
                        break;
                    }
                }

                if (!findId)
                    columns.add(idField);
                distinctFields = StringUtil.join(",", columns);
            }
            return String.format("DISTINCT ON (%s) ", distinctFields);
        } else {
            return "DISTINCT ";
        }
    }

    public static String getTableSql(DataTable target, QueryLevel level, SqlDefinition definition, ILockSql lockSql) {

        StringBuilder sql = new StringBuilder();
        StringUtil.appendLine(sql, getFromSql(target));
        StringUtil.append(sql, getJoinSql(target, definition));
        if (!definition.condition().isEmpty()) {
            StringUtil.appendFormat(sql, " WHERE %s", definition.condition().code());
        }

        return String.format("%s%s", sql, lockSql.get(level));
    }

//	#region 得到select语句

    /**
     * 获取表 {@code chainRoot} 需要查询的select字段
     *
     * @param chainRoot
     * @param exp
     * @return
     */
    public static String getSelectFieldsSql(DataTable chainRoot, SqlDefinition exp) {
        StringBuilder sql = new StringBuilder();
        sql.append(getChainRootSelectFieldsSql(chainRoot, exp).trim());

        var index = new TempDataTableIndex();
        sql.append(getSlaveSelectFieldsSql(chainRoot, chainRoot, exp, index).trim());
        StringUtil.removeLast(sql); // 移除最后一个逗号
        return sql.toString();
    }

    /**
     * 填充查询链中根表的select的字段
     *
     * @param chainRoot
     * @param exp
     * @return
     */
    private static String getChainRootSelectFieldsSql(DataTable chainRoot, SqlDefinition exp) {
        StringBuilder sql = new StringBuilder();
        fillChainRootSelectFieldsSql(chainRoot, exp, sql);
        return sql.toString();
    }

    private static void fillChainRootSelectFieldsSql(DataTable current, SqlDefinition exp, StringBuilder sql) {
        StringUtil.appendLine(sql);

        for (var field : current.fields()) {
            if (field.isAdditional())
                continue; // 不输出附加字段，有这类需求请自行编码sql语句，因为附加字段的定制化需求统一由数据映射器处理
            if (field.tip().lazy() && !exp.specifiedField(field.name()))
                continue;

            if (!containsField(field.name(), exp))
                continue;

            StringUtil.appendMessageFormat(sql, "{0}.{1} as {1},", SqlStatement.qualifier(current.name()),
                    SqlStatement.qualifier(field.name()));
        }
    }

    /**
     * * 填充查询链中从表的select的字段
     *
     * @param chainRoot
     * @param master
     * @param exp
     * @param index
     * @return
     */
    private static String getSlaveSelectFieldsSql(DataTable chainRoot, DataTable master, SqlDefinition exp,
                                                  TempDataTableIndex index) {
        StringBuilder sql = new StringBuilder();
        fillChildSelectFieldsSql(chainRoot, master, exp, sql, index);
        return sql.toString();
    }

    private static void fillChildSelectFieldsSql(DataTable chainRoot, DataTable master, SqlDefinition exp,
                                                 StringBuilder sql, TempDataTableIndex index) {
        for (var child : master.buildtimeChilds()) {
            if (!index.tryAdd(child))
                continue; // 防止由于循环引用导致的死循环

            fillFieldsSql(chainRoot, master, child, exp, sql, index);
        }
    }

    private static void fillFieldsSql(DataTable chainRoot, DataTable master, DataTable current, SqlDefinition exp,
                                      StringBuilder sql, TempDataTableIndex index) {
        if (!containsSelectTable(chainRoot, exp, current))
            return;

        var chain = current.getChainPath(chainRoot);
        boolean containsInner = exp.containsInner(chain);

        StringUtil.appendLine(sql);

        for (var field : current.fields()) {
            if (field.isAdditional())
                continue; // 不输出附加字段，有这类需求请自行编码sql语句，因为附加字段的定制化需求统一由数据映射器处理
            if (field.tip().lazy() && !exp.specifiedField(field.name()))
                continue;

            var fieldName = String.format("%s_%s", chain, field.name());

            if (!containsInner && !containsField(fieldName, exp))
                continue;

            StringUtil.appendFormat(sql, "%s.%s as %s,", SqlStatement.qualifier(chain),
                    SqlStatement.qualifier(field.name()), SqlStatement.qualifier(fieldName));
        }

        fillChildSelectFieldsSql(chainRoot, current, exp, sql, index);
    }

//	region 获取from语句

    private static String getFromSql(DataTable chainRoot) {
        return SqlStatement.qualifier(chainRoot.name());
    }

//	region 获取join语句

    private static String getJoinSql(DataTable chainRoot, SqlDefinition exp) {
        StringBuilder sql = new StringBuilder();
        var index = new TempDataTableIndex();
        fillJoinSql(chainRoot, exp, sql, index);

        return sql.toString();
    }

    private static void fillJoinSql(DataTable chainRoot, SqlDefinition exp, StringBuilder sql,
                                    TempDataTableIndex index) {
        fillChildJoinSql(StringUtil.empty(), chainRoot, chainRoot, exp, sql, index);
    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="chainRoot">是查询的根表</param>
    /// <param name="master"></param>
    /// <param name="exp"></param>
    /// <param name="masterProxyName"></param>
    /// <param name="sql"></param>
    private static void fillChildJoinSql(String currentChain, DataTable chainRoot, DataTable master, SqlDefinition exp, StringBuilder sql,
                                         TempDataTableIndex index) {

        for (var child : master.buildtimeChilds()) {
            if (!index.tryAdd(child))
                continue; // 防止由于循环引用导致的死循环

            fillJoinSql(chainRoot, master, child, currentChain, exp, sql, index);
        }
    }

    private static String combineChain(String chain0, String chain1) {
        if (chain0.isEmpty() && !chain1.isEmpty()) {
            return chain1;
        } else if (!chain0.isEmpty() && !chain1.isEmpty()) {
            return String.format("%s_%s", chain0, chain1);
        }
        return chain0;
    }


    private static void fillJoinSql(DataTable chainRoot, DataTable master, DataTable current, String rootChain,
                                    SqlDefinition exp, StringBuilder sql, TempDataTableIndex index) {
        var masterChain = current.getChainPath(master);  //master 到 current的路径
        var currentChain = combineChain(rootChain, masterChain); //root 到 current的路径

        if (!containsJoinTable(currentChain, chainRoot, exp, current))
            return;
        String masterTableName = StringUtil.isNullOrEmpty(rootChain) ? master.name() : rootChain;

        if (!sql.isEmpty())
            StringUtil.appendLine(sql);

        if (current.isMultiple()) {
            var middle = current.middle();
            var masterIdName = middle.root().equals(middle.master()) ? GeneratedField.RootIdName
                    : GeneratedField.MasterIdName;

            if (current.type() == DataTableType.AggregateRoot) {
                StringUtil.appendMessageFormat(sql,
                        " LEFT JOIN {0} on {0}.{1}={2}.Id left join {3} as {4} on {0}.{5}={4}.Id",
                        SqlStatement.qualifier(middle.name()), SqlStatement.qualifier(masterIdName),
                        SqlStatement.qualifier(masterTableName), SqlStatement.qualifier(current.name()),
                        SqlStatement.qualifier(currentChain), SqlStatement.qualifier(GeneratedField.SlaveIdName));

            } else if (current.type() == DataTableType.Middle) {

                if (current.master().equals(current.root())) {
                    StringUtil.appendMessageFormat(sql,
                            " LEFT JOIN {0} as {3} on {3}.{1}={2}.Id",
                            SqlStatement.qualifier(middle.name()), SqlStatement.qualifier(masterIdName),
                            SqlStatement.qualifier(masterTableName), SqlStatement.qualifier(currentChain));
                } else {
                    StringUtil.appendMessageFormat(sql,
                            " LEFT JOIN {0} on {0}.{1}={2}.Id left join {3} as {4} on {0}.{5}={4}.Id",
                            SqlStatement.qualifier(middle.name()), SqlStatement.qualifier(masterIdName),
                            SqlStatement.qualifier(masterTableName), SqlStatement.qualifier(current.name()),
                            SqlStatement.qualifier(currentChain), SqlStatement.qualifier(GeneratedField.SlaveIdName));
                }

            } else {
                // 中间的查询会多一个{4}.{6}={2}.Id的限定，
                StringUtil.appendMessageFormat(sql,
                        " LEFT JOIN {0} on {0}.{1}={2}.Id LEFT JOIN {3} as {4} on {0}.{5}={4}.Id and {4}.{6}={2}.Id",
                        SqlStatement.qualifier(middle.name()), SqlStatement.qualifier(masterIdName),
                        SqlStatement.qualifier(masterTableName), SqlStatement.qualifier(current.name()),
                        SqlStatement.qualifier(currentChain), SqlStatement.qualifier(GeneratedField.SlaveIdName),
                        SqlStatement.qualifier(GeneratedField.RootIdName));
            }
        } else {
            if (current.type() == DataTableType.AggregateRoot) {
                var tip = current.memberPropertyTip();
                StringUtil.appendMessageFormat(sql, " LEFT JOIN {0} as {1} on {2}.{3}={1}.Id",
                        SqlStatement.qualifier(current.name()), SqlStatement.qualifier(currentChain),
                        SqlStatement.qualifier(masterTableName), SqlStatement.qualifier(tip.name() + "Id"));
            } else {
                if (chainRoot.type() == DataTableType.AggregateRoot) {
                    var chainRootMemberPropertyTip = current.chainRoot().memberPropertyTip();
                    // string rootTableName = chainRoot.Name;
                    String rootTableName = chainRootMemberPropertyTip == null ? chainRoot.name()
                            : chainRootMemberPropertyTip.name();
                    var tip = current.memberPropertyTip();
                    StringUtil.appendMessageFormat(sql,
                            " LEFT JOIN {0} as {1} on {2}.{3}={1}.Id and {1}.{4}={5}.Id",
                            SqlStatement.qualifier(current.name()), SqlStatement.qualifier(currentChain),
                            SqlStatement.qualifier(masterTableName), SqlStatement.qualifier(tip.name() + "Id"),
                            SqlStatement.qualifier(GeneratedField.RootIdName), SqlStatement.qualifier(rootTableName));
                } else {
                    // 查询不是从根表发出的，而是从引用表，那么直接用@RootId来限定
                    var tip = current.memberPropertyTip();
                    StringUtil.appendMessageFormat(sql, " LEFT JOIN {0} as {1} on {2}.{3}={1}.Id and {1}.{4}=@{5}",
                            SqlStatement.qualifier(current.name()), SqlStatement.qualifier(currentChain),
                            SqlStatement.qualifier(masterTableName), SqlStatement.qualifier(tip.name() + "Id"),
                            SqlStatement.qualifier(GeneratedField.RootIdName), GeneratedField.RootIdName);
                }

            }

        }

        fillChildJoinSql(currentChain, chainRoot, current, exp, sql, index);
    }

    private static boolean containsField(String fieldName, SqlDefinition exp) {
        if (exp.isSpecifiedField()) {
            return exp.containsField(fieldName);
        }
        return true;
    }

    private static boolean containsSelectTable(DataTable root, SqlDefinition exp, DataTable target) {
        var path = target.getChainPath(root);

        if (target.isMultiple()) {
            return exp.containsSelectChain(path);
        }
        var tip = target.memberPropertyTip();

        if (exp.isSpecifiedField()) {
            // 指定了加载字段，那么就看表是否提供了相关的字段
            return exp.containsChain(path);
        } else {
            if (target.type() == DataTableType.AggregateRoot || tip.lazy()) {
                if (!exp.containsChain(path)) {
                    return false; // 默认情况下外部的内聚根、懒惰加载不连带查询
                }
            }
            return true;
        }
    }

    private static boolean containsJoinTable(String path, DataTable root, SqlDefinition exp, DataTable target) {
//        var path = target.getChainPath(root);
        boolean containsInner = exp.containsInner(path);

        if (containsInner)
            return true;

        if (target.isMultiple()) {
            return exp.containsChain(path);
        }
        var tip = target.memberPropertyTip();

        if (exp.isSpecifiedField()) {
            // 指定了加载字段，那么就看表是否提供了相关的字段
            return exp.containsChain(path);
        } else {
            if (target.type() == DataTableType.AggregateRoot || tip.lazy()) {
                if (!exp.containsChain(path)) {
                    return false; // 默认情况下外部的内聚根、懒惰加载不连带查询
                }
            }
            return true;
        }
    }

    // 获取最终的输出代码
    private static String getFinallyObjectSql(String tableSql, DataTable table, SqlDefinition exp, QueryLevel level) {

//        StringBuilder sb = new StringBuilder();
//        StringUtil.appendFormat(sb, "WITH %s AS (", SqlStatement.qualifier(table.name()));
//        StringUtil.appendLine(sb);
//        StringUtil.appendLine(sb, DBUtil.format(tableSql, table, level));
//        StringUtil.append(sb, ")");


        StringBuilder sb = new StringBuilder();
        StringUtil.append(sb, "(");
        StringUtil.appendLine(sb);
        StringUtil.appendLine(sb, DBUtil.format(tableSql, table, level));
        StringUtil.appendFormat(sb, ") AS %s", SqlStatement.qualifier(table.name()));

        return sb.toString();
    }

    //endregion
}
