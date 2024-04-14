package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.MapData;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.util.StringUtil;

/**
 * 基于表达式的查询,可以指定对象属性等表达式
 * 
 * select子语句系统内部使用，外部请不要调用
 */
public abstract class QueryExpression implements IQueryBuilder {

	/**
	 * 查询的锁定级别
	 * 
	 * @return
	 */
	public QueryLevel level() {
		return _level;
	}

	public String getName() {
		return _definition.key();
	}

	public QueryExpression(DataTable target, String expression, QueryLevel level) {
		_target = target;
		_ _definition = SqlDefinition.create(_expression);
		_level = level;
	}

	@Override
	public String build(QueryDescription description) {
		// 表达式针对的目标表
		var target = description.table();
		// 对象表达式
		String expression = description.getItem("expression");
		// 查询级别
		QueryLevel level = description.getItem("queryLevel");
		var definition = SqlDefinition.create(expression);

		var commandText = getCommandText(description);

		return definition.process(commandText, description.param());
	}

	/**
	 * 获取命令文本
	 * 
	 * @param description
	 * @return
	 */
	protected abstract String getCommandText(QueryDescription description);

	protected String getObjectSql(DataTable target, QueryLevel level, SqlDefinition definition) {

		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		StringUtil.appendLine(sql, getSelectFieldsSql(target, definition));
		StringUtil.appendLine(sql, " from ");
		StringUtil.appendLine(sql, getFromSql(target, level, definition));
		StringUtil.appendLine(sql, getJoinSql(target, definition));

		return GetFinallyObjectSql(sql.ToString(), table);
	}

//	#region 得到select语句

	/**
	 * 
	 * 获取表 {@code chainRoot} 需要查询的select字段
	 * 
	 * @param chainRoot
	 * @param exp
	 * @return
	 */
	private static String getSelectFieldsSql(DataTable chainRoot, SqlDefinition exp) {
		StringBuilder sql = new StringBuilder();
		sql.append(getChainRootSelectFieldsSql(chainRoot, exp).trim());

		var index = new TempDataTableIndex();
		sql.Append(getSlaveSelectFieldsSql(chainRoot, chainRoot, exp, index).trim());
		StringUtil.removeLast(sql); // 移除最后一个逗号
		return sql.toString();
	}

	/**
	 * 
	 * 填充查询链中根表的select的字段
	 * 
	 * @param chainRoot
	 * @param exp
	 * @return
	 */
	private static String getChainRootSelectFieldsSql(DataTable chainRoot, SqlDefinition exp) {
		StringBuilder sql = new StringBuilder();
		FillChainRootSelectFieldsSql(chainRoot, TableType.Common, exp, sql);
		return sql.toString();
	}

	private static void FillChainRootSelectFieldsSql(DataTable current, TableType tableType, SqlDefinition exp,
			StringBuilder sql) {
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

			fillFieldsSql(chainRoot, master, child, TableType.Common, exp, sql, index);
		}
	}

	private static void fillFieldsSql(DataTable chainRoot, DataTable master, DataTable current, TableType tableType,
			SqlDefinition exp, StringBuilder sql, TempDataTableIndex index) {
		if (!containsTable(chainRoot, exp, current))
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

	private static String getFromSql(DataTable chainRoot, QueryLevel level, SqlDefinition exp) {
		return String.format(" %s%s", SqlStatement.qualifier(chainRoot.name()), getLockCode(level));
	}

//	region 获取join语句

	private static String getJoinSql(DataTable chainRoot, SqlDefinition exp)
    {
        StringBuilder sql = new StringBuilder();
        using (var temp = TempIndex.Borrow())
        {
            var index = temp.Item;
            FillJoinSql(chainRoot, chainRoot, exp, sql, index);
        }

        return sql.ToString();
    }

	private static void FillJoinSql(DataTable chainRoot, DataTable master, SqlDefinition exp, StringBuilder sql,TempIndex index)
    {
        if(master.IsDerived)
        {
            var inheritedRoot = master.InheritedRoot;
            FillChildJoinSql(chainRoot, inheritedRoot, exp, sql, index);
            foreach (var derived in master.Deriveds)
            {
                FillChildJoinSql(chainRoot, derived, exp, sql, index);
            }
        }
        else
        {
            FillChildJoinSql(chainRoot, chainRoot, exp, sql, index);
        }
    }

	/// <summary>
	///
	/// </summary>
	/// <param name="chainRoot">是查询的根表</param>
	/// <param name="master"></param>
	/// <param name="exp"></param>
	/// <param name="masterProxyName"></param>
	/// <param name="sql"></param>
	private static void FillChildJoinSql(DataTable chainRoot, DataTable master, SqlDefinition exp, StringBuilder sql, TempIndex index)
    {
        var masterChain = master.GetChainCode(chainRoot);

        foreach (var child in master.BuildtimeChilds)
        {
            if (!index.TryAdd(child)) continue; //防止由于循环引用导致的死循环

            if (child.IsDerived)
            {
                FillJoinSqlByDerived(chainRoot, master, child, masterChain, exp, sql, index);
            }
            else
            {
                FillJoinSqlByNoDerived(chainRoot, master, child, masterChain, exp, sql, index);
            }
        }
    }

	private static void FillJoinSqlByDerived(DataTable chainRoot, DataTable master, DataTable current,
			string masterChain, SqlDefinition exp, StringBuilder sql, TempIndex index) {
		if (!ContainsTable(chainRoot, exp, current))
			return;

		var chain = current.GetChainCode(chainRoot);
		var childSql = GetDerivedTableSql(current, QueryLevel.None);
		string masterTableName = string.IsNullOrEmpty(masterChain) ? master.Name : masterChain;

		sql.AppendLine();

		var tip = current.MemberPropertyTip;
		sql.AppendFormat(" left join ({0}) as {1}{4} on {2}.{3}Id={1}.Id", childSql, SqlStatement.Qualifier(chain),
				SqlStatement.Qualifier(masterTableName), tip.PropertyName, GetLockCode(QueryLevel.None));

		FillJoinSql(chainRoot, current, exp, sql, index);
	}

	private static void FillJoinSqlByNoDerived(DataTable chainRoot, DataTable master, DataTable current,
			string masterChain, SqlDefinition exp, StringBuilder sql, TempIndex index) {
		if (!ContainsTable(chainRoot, exp, current))
			return;
		var chain = current.GetChainCode(chainRoot);
		string masterTableName = string.IsNullOrEmpty(masterChain) ? master.Name : masterChain;

		string currentTenantSql = current.IsSessionEnabledMultiTenancy
				? string.Format("and {0}.{1}=@{2}", SqlStatement.Qualifier(chain),
						SqlStatement.Qualifier(GeneratedField.TenantIdName), GeneratedField.TenantIdName)
				: string.Empty;

		sql.AppendLine();
		if (current.IsMultiple) {
			var middle = current.Middle;
			var masterIdName = middle.Root == middle.Master ? GeneratedField.RootIdName : GeneratedField.MasterIdName;

			string middleTenantSql = middle.IsSessionEnabledMultiTenancy
					? string.Format("and {0}.{1}=@{2}", SqlStatement.Qualifier(middle.Name),
							SqlStatement.Qualifier(GeneratedField.TenantIdName), GeneratedField.TenantIdName)
					: string.Empty;

			if (current.Type == DataTableType.AggregateRoot) {
				sql.AppendFormat(
						" left join {0}{6} on {0}.{1}={2}.Id {7} left join {3} as {4}{6} on {0}.{5}={4}.Id {8}",
						SqlStatement.Qualifier(middle.Name), SqlStatement.Qualifier(masterIdName),
						SqlStatement.Qualifier(masterTableName), SqlStatement.Qualifier(current.Name),
						SqlStatement.Qualifier(chain), GeneratedField.SlaveIdName, GetLockCode(QueryLevel.None),
						middleTenantSql, currentTenantSql);
			} else {
				// 中间的查询会多一个{4}.{6}={2}.Id的限定，
				sql.AppendFormat(
						" left join {0}{7} on {0}.{1}={2}.Id {8} left join {3} as {4}{7} on {0}.{5}={4}.Id and {4}.{6}={2}.Id {9}",
						SqlStatement.Qualifier(middle.Name), SqlStatement.Qualifier(masterIdName),
						SqlStatement.Qualifier(masterTableName), SqlStatement.Qualifier(current.Name),
						SqlStatement.Qualifier(chain), GeneratedField.SlaveIdName, GeneratedField.RootIdName,
						GetLockCode(QueryLevel.None), middleTenantSql, currentTenantSql);
			}
		} else {
			if (current.Type == DataTableType.AggregateRoot) {
				var tip = current.MemberPropertyTip;
				sql.AppendFormat(" left join {0} as {1}{4} on {2}.{3}Id={1}.Id {5}",
						SqlStatement.Qualifier(current.Name), SqlStatement.Qualifier(chain),
						SqlStatement.Qualifier(masterTableName), tip.PropertyName, GetLockCode(QueryLevel.None),
						currentTenantSql);
			} else {
				if (chainRoot.Type == DataTableType.AggregateRoot) {
					var chainRootMemberPropertyTip = current.ChainRoot.MemberPropertyTip;
					// string rootTableName = chainRoot.Name;
					string rootTableName = chainRootMemberPropertyTip == null ? chainRoot.Name
							: chainRootMemberPropertyTip.PropertyName;
					var tip = current.MemberPropertyTip;
					sql.AppendFormat(" left join {0} as {1}{4} on {2}.{3}Id={1}.Id and {1}.{5}={6}.Id {7}",
							SqlStatement.Qualifier(current.Name), SqlStatement.Qualifier(chain),
							SqlStatement.Qualifier(masterTableName), tip.PropertyName, GetLockCode(QueryLevel.None),
							GeneratedField.RootIdName, SqlStatement.Qualifier(rootTableName), currentTenantSql);
				} else {
					// 查询不是从根表发出的，而是从引用表，那么直接用@RootId来限定
					var tip = current.MemberPropertyTip;
					sql.AppendFormat(" left join {0} as {1}{4} on {2}.{3}Id={1}.Id and {1}.{5}=@{5} {6}",
							SqlStatement.Qualifier(current.Name), SqlStatement.Qualifier(chain),
							SqlStatement.Qualifier(masterTableName), tip.PropertyName, GetLockCode(QueryLevel.None),
							GeneratedField.RootIdName, currentTenantSql);
				}

			}

		}

		FillChildJoinSql(chainRoot, current, exp, sql, index);
	}

	#endregion

	#

	region 其他辅助方法

	private enum TableType {
		InheritedRoot, Derived, Common
	}

	public static string GetLockCode(QueryLevel level) {
		var agent = SqlContext.GetAgent();
		if (agent.Database == DatabaseType.SQLServer) {
			return SQLServer.SqlStatement.GetLockCode(level);
		}
		throw new NotSupportDatabaseException("GetLockCode", agent.Database);
	}

	private static string GetDerivedTableSql(DataTable table, QueryLevel level)
    {
        var inheritedRoot = table.InheritedRoot;

        StringBuilder sql = new StringBuilder();
        sql.Append("select ");
        sql.Append(GetChainRootSelectFieldsSql(table, SqlDefinition.All));
        sql.Length--;
        sql.AppendLine();
        sql.AppendFormat(" from {0}{1}", SqlStatement.Qualifier(inheritedRoot.Name), GetLockCode(level));
        foreach (var derived in table.Deriveds)
        {
            string derivedTenantSql = derived.IsSessionEnabledMultiTenancy ? string.Format("and {0}.{1}=@{2}", SqlStatement.Qualifier(derived.Name),
                                                            SqlStatement.Qualifier(GeneratedField.TenantIdName),
                                                            GeneratedField.TenantIdName) : string.Empty;

            if (table.Type == DataTableType.AggregateRoot)
            {
                sql.AppendFormat(" inner join {0}{2} on {1}.Id={0}.Id {3}",
                    SqlStatement.Qualifier(derived.Name), 
                    SqlStatement.Qualifier(inheritedRoot.Name), 
                    GetLockCode(QueryLevel.None), 
                    derivedTenantSql);
            }
            else
            {
                sql.AppendFormat(" inner join {0}{3} on {1}.Id={0}.Id and {1}.{2}={0}.{2} {4}",
                    SqlStatement.Qualifier(derived.Name),
                    SqlStatement.Qualifier(inheritedRoot.Name),
                    SqlStatement.Qualifier(GeneratedField.RootIdName), 
                    GetLockCode(QueryLevel.None), 
                    derivedTenantSql);
            }
        }
        return sql.ToString();
    }

	/// <summary>
	/// 获取派生类table的完整代码，该代码可获取整个派生类的信息
	/// </summary>
	/// <param name="table"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	// private static string GetDerivedTableSql(DataTable table, QueryLevel level)
	// {
	// var inheritedRoot = table.InheritedRoot;

	// StringBuilder sql = new StringBuilder();
	// sql.Append("select ");
	// sql.AppendLine(GetSelectFieldsSql(table, SqlDefinition.All));
	// sql.AppendFormat(" from {0}{1}", inheritedRoot.Name, GetLockCode(level));
	// FillJoinSql(inheritedRoot, inheritedRoot, SqlDefinition.All, sql);
	// foreach (var derived in table.Deriveds)
	// {
	// if (table.Type == DataTableType.AggregateRoot)
	// {
	// sql.AppendFormat(" inner join {0} on {1}.Id={0}.Id",
	// derived.Name, inheritedRoot.Name);
	// }
	// else
	// {
	// sql.AppendFormat(" inner join {0} on {1}.Id={0}.Id and {1}.{2}={0}.{2}",
	// derived.Name, inheritedRoot.Name, GeneratedField.RootIdName);
	// }

	// FillJoinSql(derived, derived, SqlDefinition.All, sql);
	// }
	// return sql.ToString();
	// }

	private static bool ContainsField(string fieldName, SqlDefinition exp) {
		if (exp.IsSpecifiedField) {
			return exp.ContainsField(fieldName);
		}
		return true;
	}

	private static bool ContainsTable(DataTable root, SqlDefinition exp, DataTable target) {
		var path = target.GetChainCode(root);
		bool containsInner = exp.ContainsInner(path);

		if (containsInner)
			return true;

		if (target.IsMultiple) {
			return exp.ContainsChain(path);
		}
		var tip = target.MemberPropertyTip;

		if (exp.IsSpecifiedField) {
			// 指定了加载字段，那么就看表是否提供了相关的字段
			return exp.ContainsChain(path);
		} else {
			if (target.Type == DataTableType.AggregateRoot || tip.Lazy) {
				if (!exp.ContainsChain(path)) {
					return false; // 默认情况下外部的内聚根、懒惰加载不连带查询
				}
			}
			return true;
		}
	}

	// 获取最终的输出代码
	private string GetFinallyObjectSql(string tableSql, DataTable table)
    {
        string sql = null;

        if(this.Definition.HasInner)
        {
            if (this.Definition.Condition.IsEmpty())
            {
                sql = string.Format("select distinct * from ({0}) as {1}", tableSql, SqlStatement.Qualifier(table.Name));
            }
            else
            {
                sql = string.Format("select distinct * from ({0}) as {1} where {2}", tableSql, SqlStatement.Qualifier(table.Name), this.Definition.Condition.Code);
            }
        }
        else
        {
            if (this.Definition.Condition.IsEmpty())
            {
                sql = string.Format("select distinct {2} from ({0}) as {1}", tableSql, SqlStatement.Qualifier(table.Name), this.Definition.GetFieldsSql());
            }
            else
            {
                sql = string.Format("select distinct {3} from ({0}) as {1} where {2}", tableSql, SqlStatement.Qualifier(table.Name), this.Definition.Condition.Code, this.Definition.GetFieldsSql());
            }
        }

        return string.Format("({0}) as {1}", sql, SqlStatement.Qualifier(table.Name));
    }

	#endregion
}}
