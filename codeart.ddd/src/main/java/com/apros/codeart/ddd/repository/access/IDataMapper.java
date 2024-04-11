package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.metadata.ObjectMeta;

public interface IDataMapper {

	Iterable<IDataField> getObjectFields(ObjectMeta meta);

	// void FillInsertData(DomainObject obj, DynamicData data, DataTable table);

	void onPreInsert(DomainObject obj, DataTable table);

	void onInserted(DomainObject obj, DataTable table);

	// void FillUpdateData(DomainObject obj, DynamicData data, DataTable table);

	void onPreUpdate(DomainObject obj, DataTable table);

	void onUpdated(DomainObject obj, DataTable table);

	void onPreDelete(DomainObject obj, DataTable table);

	void onDeleted(DomainObject obj, DataTable table);

	/**
	 * 构建查询命令
	 * 
	 * @param builder
	 * @param descripton
	 * @return
	 */
	String build(IQueryBuilder builder, QueryDescription descripton);
}
