package apros.codeart.ddd.repository;

import java.util.function.Consumer;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataPortal;

public abstract class SqlRepository<TRoot extends IAggregateRoot> extends AbstractRepository<TRoot> {

//	#region 增删改

	@Override
	protected void persistAddRoot(TRoot obj) {
		DataContext.using(() -> {
			DataPortal.insert((DomainObject) obj);
		});
	}

	@Override
	protected void persistUpdateRoot(TRoot obj) {
		DataContext.using(() -> {
			DataPortal.update((DomainObject) obj);
		});
	}

	@Override
	protected void persistDeleteRoot(TRoot obj) {
		DataContext.using(() -> {
			DataPortal.delete((DomainObject) obj);
		});
	}

//	#endregion

	@Override
	protected TRoot persistFind(Object id, QueryLevel level) {
		return DataPortal.querySingle(this.getRootType(), id, level);
	}

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	protected <T extends IAggregateRoot> T querySingle(Class<T> objectType, String expression,
			Consumer<MapData> fillArg, QueryLevel level) {

		return DataContext.using(() -> {
			var dataContext = DataContext.getCurrent();
			// 执行查询，并且向数据上下文中注册查询结果
			return dataContext.registerQueried(objectType, level, () -> {
				return DataPortal.querySingle(objectType, expression, fillArg, level);
			});
		});
	}

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	public <T extends IAggregateRoot> Iterable<T> query(Class<T> objectType, String expression,
			Consumer<MapData> fillArg, QueryLevel level) {
		return DataContext.using(() -> {
			var dataContext = DataContext.getCurrent();
			// 执行查询，并且向数据上下文中注册查询结果
			return dataContext.registerCollectionQueried(objectType, level, () -> {
				return DataPortal.query(objectType, expression, fillArg, level);
			});

		});
	}

	/// <summary>
	/// 基于对象表达式的查询
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="expression"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	public <T extends IAggregateRoot> Page<T> query(Class<T> objectType, String expression, int pageIndex, int pageSize,
			Consumer<MapData> fillArg, QueryLevel level) {
		return DataContext.using(() -> {
			var dataContext = DataContext.getCurrent();
			return dataContext.registerPageQueried(objectType, level, () -> {
				return DataPortal.query(objectType, expression, pageIndex, pageSize, fillArg);
			});

		});
	}

	public <T extends IAggregateRoot> int getCount(Class<T> objectType, String expression, Consumer<MapData> fillArg,
			QueryLevel level) {
		return DataContext.using(() -> {
			return DataPortal.getCount(objectType, expression, fillArg, level);
		});
	}

}
