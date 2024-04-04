package com.apros.codeart.ddd.repository.access;

import java.sql.Connection;

public final class DataAccess {

	private Connection _conn;

	DataAccess(Connection conn) {
		_conn = conn;
	}

	public Object executeScalar(String sql, object param, QueryLevel level) {
		sql = getLevelSql(sql, level);
		return _conn.ExecuteScalar(sql, param, _tran);
	}

	public T ExecuteScalar<T>(
	string sql, object param=null,
	QueryLevel level = null)
	{
	    sql = GetLevelSql(sql, level);
	    return _conn.ExecuteScalar<T>(sql, param, _tran);
	}

	public int Execute(string sql, object param = null, QueryLevel level = null)
	{
	    sql = GetLevelSql(sql, level);
	    return _conn.Execute(sql, param, _tran);
	}

	public IEnumerable<T> ExecuteScalars<T>(
	string sql, object param=null,
	QueryLevel level = null)
	{
	    sql = GetLevelSql(sql, level);
	    return _conn.Query<T>(sql, param, _tran);
	}

	public IDataReader ExecuteReader(string sql, object param = null, QueryLevel level = null)
	{
	    sql = GetLevelSql(sql, level);
	    return _conn.ExecuteReader(sql, param, _tran);
	}

	public T QuerySingle<T>(
	string sql, object param=null,
	QueryLevel level = null)
	where T:IDataObject,new()
	{
	    sql = GetLevelSql(sql, level);
	    var obj = new T();
	    using (var reader = _conn.ExecuteReader(sql, param, _tran))
	    {
	        obj.Load(reader);
	    }
	    return obj;
	}

	public dynamic QuerySingle(string sql, object param = null, QueryLevel level = null)
	{
	    sql = GetLevelSql(sql, level);
	    return _conn.QuerySingle(sql, param, _tran);
	}

	public IEnumerable<T> Query<T>(
	string sql, object param=null,
	QueryLevel level = null)
	where T:IDataObject,new()
	{
	    sql = GetLevelSql(sql, level);
	    List<T> objs = new List<T>();
	    using (var reader = _conn.ExecuteReader(sql, param, _tran))
	    {
	        while (true)
	        {
	            var obj = new T();
	            obj.Load(reader);
	            if (obj.IsEmpty()) break;
	            objs.Add(obj);
	        }
	    }
	    return objs;
	}

	public IEnumerable<dynamic> Query(string sql, object param = null, QueryLevel level = null)
	{
	    sql = GetLevelSql(sql, level);
	    return _conn.Query(sql, param, _tran);
	}

	public dynamic QueryFirstOrDefault(string sql, object param = null, QueryLevel level = null)
	{
	    sql = GetLevelSql(sql, level);
	    return _conn.QueryFirstOrDefault(sql, param, _tran);
	}

	private static RegexPool _regexPool = new RegexPool(".+from[ ](.+?)(where|inner|left)", RegexOptions.IgnoreCase);

	private static Func<string, Func<QueryLevel, string>> _getLevelSql = LazyIndexer.Init<string, Func<QueryLevel,string>>((sql)=>
	{
	     return LazyIndexer.Init<QueryLevel, string>((level) ->
	     {
	         using (var temp = _regexPool.Borrow())
	         {
	             var reg = temp.Item;
	             var math = reg.Match(sql);
	             if (!math.Success) throw new DomainEventException("解析level错误");
	             var tableName = math.Groups[1].Value;
	             var index = math.Groups[1].Index;
	             return sql.Insert(index + tableName.Length, level.GetMSSqlLockCode());
	         }
	     });
	 });

	private static string GetLevelSql(string sql, QueryLevel level)
	{
	    if (level == null) return sql;
	    return _getLevelSql(sql)(level);
	}
}
