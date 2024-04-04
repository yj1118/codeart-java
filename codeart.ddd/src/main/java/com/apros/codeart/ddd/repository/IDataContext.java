package com.apros.codeart.ddd.repository;

import java.util.function.Supplier;

import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.QueryLevel;

public interface IDataContext extends AutoCloseable {

//	region 事务管理

	/**
	 * 
	 * 表示当前数据上下文的实例是否正在事务模式中
	 * 
	 * @return
	 */
	boolean inTransaction();

	/**
	 * 开始进入事务模式
	 */
	void beginTransaction();

	/**
	 * 提交所有执行计划
	 */
	void commit();

	/**
	 * 抛弃所有未提交的执行计划，并重置事务模式
	 */
	void rollback();

	/**
	 * 注册回滚项，外界可以指定回滚内容
	 * 
	 * @param e
	 */
	void registerRollback(RepositoryRollbackEventArgs e);

	/**
	 * 执行计划中是否有未提交的单元
	 * 
	 * @return
	 */
	boolean isDirty();

	/**
	 * 是否正在提交事务
	 * 
	 * @return
	 */
	boolean isCommiting();

//	region 工作单元

	<T extends IAggregateRoot> void registerAdded(T item, IPersistRepository repository);

	<T extends IAggregateRoot> void registerUpdated(T item, IPersistRepository repository);

	<T extends IAggregateRoot> void registerDeleted(T item, IPersistRepository repository);

	/**
	 * 
	 * 向数据上下文注册查询，该方法会控制锁和同步查询结果
	 * 
	 * @param <T>
	 * @param level
	 * @param persistQuery
	 * @return
	 */
	<T extends IAggregateRoot> T registerQueried(Class<T> objectType, QueryLevel level, Supplier<T> persistQuery);

	/**
	 * 向数据上下文注册集合查询，该方法会控制锁和同步查询结果
	 * 
	 * @param <T>
	 * @param level
	 * @param persistQuery
	 * @return
	 */
	<T extends IAggregateRoot> Iterable<T> registerCollectionQueried(Class<T> objectType, QueryLevel level,
			Supplier<Iterable<T>> persistQuery);

	/**
	 * 
	 * 向数据上下文注册翻页查询，该方法会控制锁和同步查询结果
	 * 
	 * @param <T>
	 * @param objectType
	 * @param level
	 * @param persistQuery
	 * @return
	 */
	<T extends IAggregateRoot> Page<T> registerPageQueried(Class<T> objectType, QueryLevel level,
			Supplier<Page<T>> persistQuery);

	/**
	 * 开启锁
	 * 
	 * @param level
	 */
	void openLock(QueryLevel level);

}
