package com.apros.codeart.ddd.repository.access;

import static com.apros.codeart.runtime.Util.propagate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

import com.apros.codeart.ddd.Dictionary;
import com.apros.codeart.ddd.repository.access.QueryFilter.IQueryFilter;
import com.apros.codeart.ddd.repository.access.QueryFilter.Row;
import com.apros.codeart.ddd.repository.access.QueryFilter.Rows;
import com.apros.codeart.dto.DTObject;
import com.apros.codeart.util.LazyIndexer;
import com.google.common.collect.Iterables;

public final class QueryRunner {
	private QueryRunner() {

	}

	public static int execute(Connection conn, String sql, Dictionary param) {

		try (PreparedStatement pstmt = getStatement(conn, sql, param);) {
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			throw propagate(e);
		}
	}

	public static Object queryScalar(Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.Scalar();
		query(conn, sql, param, filter);
		return filter.result();
	}

	@SuppressWarnings("unchecked")
	public static <T> T queryScalar(Class<T> valueType, Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.Scalar();
		query(conn, sql, param, filter);
		return (T) filter.result();
	}

	public static int queryScalarInt(Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.ScalarInt();
		query(conn, sql, param, filter);
		return filter.result();
	}

	public static UUID queryScalarGuid(Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.ScalarGuid();
		query(conn, sql, param, filter);
		return filter.result();
	}

	public static Iterable<Object> queryScalars(Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.Scalars<Object>();
		query(conn, sql, param, filter);
		return filter.result();
	}

	public static <T> Iterable<T> queryScalars(Class<T> elementType, Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.Scalars<T>();
		query(conn, sql, param, filter);
		return filter.result();
	}

	public static int[] queryScalarInts(Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.ScalarInts();
		query(conn, sql, param, filter);
		return filter.result();
	}

	public static DTObject queryDTO(Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.DTO();
		query(conn, sql, param, filter);
		return filter.result();
	}

	public static Iterable<DTObject> queryDTOs(Connection conn, String sql, Dictionary param) {
		var filter = new QueryFilter.DTOs();
		query(conn, sql, param, filter);
		return filter.result();
	}

	/**
	 * 
	 * 多行
	 * 
	 * @param conn
	 * @param sql
	 * @param param
	 * @return
	 */
	public Dictionary queryRow(Connection conn, String sql, Dictionary param) {
		var filter = new Row();
		query(conn, sql, param, filter);
		return filter.result();
	}

	public Iterable<Dictionary> queryRows(Connection conn, String sql, Dictionary param) {
		var filter = new Rows();
		query(conn, sql, param, filter);
		return filter.result();
	}

	private static void query(Connection conn, String sql, Dictionary param, IQueryFilter filter) {

		try (PreparedStatement pstmt = getStatement(conn, sql, param); ResultSet rs = pstmt.executeQuery()) {
			filter.extract(rs);
		} catch (SQLException e) {
			throw propagate(e);
		}
	}

	private static class QueryAdapter {

		private String _sql;

		/**
		 * 要执行的语句
		 * 
		 * @return
		 */
		public String sql() {
			return _sql;
		}

		private String[] _positions;

		public QueryAdapter(String sql, String[] positions) {
			_sql = sql;
			_positions = positions;
		}

		/**
		 * 
		 * 将键值对的参数，转换为匹配sql语句的参数数组
		 * 
		 * @param param
		 * @return
		 */
		public void fillParams(PreparedStatement statement, Dictionary param) {
			try {

				for (var i = 0; i < _positions.length; i++) {
					var name = _positions[i];
					var value = param.get(name);
					var index = i + 1; // sql里的参数是从1开始算的
					statement.setObject(index, value);
				}

			} catch (SQLException e) {
				throw propagate(e);
			}
		}

		public static QueryAdapter parse(String sql) {
			StringBuilder sb = new StringBuilder();
			ArrayList<String> positions = new ArrayList<>();
			for (int i = 0; i < sql.length(); i++) {
				char ch = sql.charAt(i);
				if (ch == '@') {
					var name = findName(sql, i);
					positions.add(name);
					i += name.length();
					sb.append("?");
					continue;
				}
				sb.append(ch);
			}

			return new QueryAdapter(sb.toString(), Iterables.toArray(positions, String.class));
		}

		private static String findName(String sql, int index) {
			StringBuilder name = new StringBuilder();
			// index 对应的是@,index+1才是真正的名称的开始
			for (int i = (index + 1); i < sql.length(); i++) {
				char ch = sql.charAt(i);
				if (ch == ' ' || ch == ';') {
					return name.toString();
				}
				name.append(ch);
			}
			return name.toString();
		}

	}

	private static Function<String, QueryAdapter> _getAdapter = LazyIndexer.init((sql) -> {
		return QueryAdapter.parse(sql);
	});

	private static PreparedStatement getStatement(Connection conn, String sql, Dictionary param) throws SQLException {
		var adapter = _getAdapter.apply(sql);
		PreparedStatement pstmt = conn.prepareStatement(adapter.sql());
		adapter.fillParams(pstmt, param);
		return pstmt;
	}
}
