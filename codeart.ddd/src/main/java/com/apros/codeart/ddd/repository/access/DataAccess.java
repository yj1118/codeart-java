package com.apros.codeart.ddd.repository.access;

import java.sql.Connection;
import java.util.UUID;

import com.apros.codeart.ddd.Dictionary;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.dto.DTObject;

public final class DataAccess {

	private Connection _conn;

	DataAccess(Connection conn) {
		_conn = conn;
	}

	public int execute(String sql, Dictionary param, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.execute(_conn, sql, param);
	}

	public Object queryScalar(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalar(_conn, sql, params);
	}

	public <T> T queryScalar(Class<T> valueType, String sql, Dictionary param, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalar(valueType, _conn, sql, param);
	}

	public int queryScalarInt(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalarInt(_conn, sql, params);
	}

	public long queryScalarLong(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalarLong(_conn, sql, params);
	}

	public long queryScalarLong(String sql) {
		return QueryRunner.queryScalarLong(_conn, sql, null);
	}

	public UUID queryScalarGuid(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalarGuid(_conn, sql, params);
	}

	public Iterable<Object> queryScalars(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalars(_conn, sql, params);
	}

	public <T> Iterable<T> queryScalars(Class<T> elementType, String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalars(elementType, _conn, sql, params);
	}

	public int[] queryScalarInts(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryScalarInts(_conn, sql, params);
	}

	public DTObject queryDTO(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryDTO(_conn, sql, params);
	}

	public Iterable<DTObject> queryDTOs(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryDTOs(_conn, sql, params);
	}

	public Dictionary queryRow(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryRow(_conn, sql, params);
	}

	public Iterable<Dictionary> queryRows(String sql, Dictionary params, QueryLevel level) {
		sql = supplementLock(sql, level);
		return QueryRunner.queryRows(_conn, sql, params);
	}

	private static String supplementLock(String sql, QueryLevel level) {
		return DataSource.getAgent().supplementLock(sql, level);
	}

}
