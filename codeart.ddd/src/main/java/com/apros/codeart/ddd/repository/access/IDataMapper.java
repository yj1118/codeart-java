package com.apros.codeart.ddd.repository.access;

import org.reflections.util.QueryBuilder;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.metadata.ObjectMeta;

public interface IDataMapper {

	Iterable<IDataField> getObjectFields(ObjectMeta meta);

	// void FillInsertData(DomainObject obj, DynamicData data, DataTable table);

	void OnPreInsert(DomainObject obj, DataTable table);

	void OnInserted(DomainObject obj, DataTable table);

	// void FillUpdateData(DomainObject obj, DynamicData data, DataTable table);

	void OnPreUpdate(DomainObject obj, DataTable table);

	void OnUpdated(DomainObject obj, DataTable table);

	void OnPreDelete(DomainObject obj, DataTable table);

	void OnDeleted(DomainObject obj, DataTable table);

	/// <summary>
	/// 构建查询命令
	/// </summary>
	/// <param name="builder"></param>
	/// <param name="param"></param>
	/// <returns></returns>
	String Build(QueryBuilder builder, DynamicData param, DataTable table);
}
