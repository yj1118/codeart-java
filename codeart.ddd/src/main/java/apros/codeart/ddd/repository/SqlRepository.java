package apros.codeart.ddd.repository;

import java.util.function.Consumer;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataPortal;

public abstract class SqlRepository<TRoot extends IAggregateRoot> extends AbstractRepository<TRoot> {

//	#region 增删改

	@Override
	protected void persistAddRoot(TRoot obj)
	 {
	     DataContext.using(() ->
	     {
	         DataPortal.Create(obj as DomainObject);
	     });
	 }

	@Override
	protected void persistUpdateRoot(TRoot obj)
	 {
	     DataContext.using(() ->
	     {
	         DataPortal.Update(obj as DomainObject);
	     });
	 }

	@Override
	protected void persistDeleteRoot(TRoot obj)
	 {
	     DataContext.Using(() ->
	     {
	         DataPortal.Delete(obj as DomainObject);
	     });
	 }

//	#endregion

	@Override
	protected TRoot persistFind(Object id, QueryLevel level) {
	     return DataPortal.QuerySingle<TRoot>(id, level);
	}

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	protected <T extends IAggregateRoot> T QuerySingle(String expression, Consumer<DynamicData> fillArg, QueryLevel level)
	{
	     T result = null;
	     DataContext.using((access) ->
	     {
	         result = access.querySingle<T>(expression, fillArg, level);
	     });
	     return result;
	 }

	// protected T QuerySingle<T>(IQueryBuilder compiler, Action<DynamicData>
	// fillArg, QueryLevel level) where T : class, IRepositoryable
	// {
	// return DataContext.Current.QuerySingle<T>(compiler, fillArg, level);
	// }

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	public IEnumerable<T> Query<T>(
	string expression, Action<DynamicData>fillArg,
	QueryLevel level)
	where T:class,IAggregateRoot
	{
	     IEnumerable<T> result = null;
	     DataContext.Using(() =>
	     {
	         result = DataContext.Current.Query<T>(expression, fillArg, level);
	     });
	     return result;
	 }

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	public Page<T> Query<T>(
	string expression,
	int pageIndex,
	int pageSize, Action<DynamicData>fillArg)
	where T:class,IAggregateRoot
	{
	     Page<T> result = default(Page<T>);
	     DataContext.Using(() =>
	     {
	         result = DataContext.Current.Query<T>(expression, pageIndex, pageSize, fillArg);
	     });
	     return result;
	 }

	public int GetCount<T>(
	string expression, Action<DynamicData>fillArg,
	QueryLevel level)
	where T:class,IAggregateRoot
	{
	     int result = 0;
	     DataContext.Using(() =>
	     {
	         result = DataContext.Current.GetCount<T>(expression, fillArg, level);
	     });
	     return result;
	 }

	/// <summary>
	/// 使用适配器查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="adapterName">适配器的名称，在类型<typeparam
	/// name="T"/>下唯一，该名称会用来提高程序性能</param>
	/// <returns></returns>
	public QueryAdapter<T> Adapter<T>(string adapterName)
	where T:class,IAggregateRoot
	{
	     QueryAdapter<T> result = null;
	     DataContext.Using(() =>
	     {
	         result = DataContext.Current.Adapter<T>(adapterName);
	     });
	     return result;
	 }

}
