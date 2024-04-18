package apros.codeart.ddd.repository.access;

import static apros.codeart.runtime.Util.propagate;

import java.sql.Connection;
import java.sql.SQLException;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.i18n.Language;

public class DataConnection implements AutoCloseable {

	private Connection _conn;

	private boolean _tranOpened;

	private DataAccess _access;

	public DataAccess access() {
		return _access;
	}

	public DataConnection() {

	}

//	region 初始化

	public void initialize() {
		_conn = createConnection();
		_tranOpened = false;
		_access = new DataAccess(_conn);
	}

	public void begin() {
		if (_tranOpened)
			throw new DomainDrivenException(Language.strings("TransactionStarted"));
		openTransaction(_conn);
		_tranOpened = true;
	}

	private static Connection createConnection() {
		return DataSource.getConnection();
	}

	private static void openTransaction(Connection conn) {
		// 事务的开始是隐式的，从你获取连接并关闭自动提交模式（
		// 通过setAutoCommit(false)）的那一刻开始。这个操作告诉数据库管理系统，
		// 接下来执行的一系列SQL语句应该被视为一个单独的事务，直到你显式地调用commit或rollback方法
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			throw propagate(e);
		}
	}

	public void rollback() {
		if (_tranOpened) {
			try {
				_conn.rollback();
			} catch (SQLException e) {
				throw propagate(e);
			}
		}
	}

	public void commit() {
		if (_tranOpened) {
			try {
				_conn.commit();
			} catch (SQLException e) {
				throw propagate(e);
			}
		}
	}

	@Override
	public void close() {
		if (_conn != null) {
			try {
				_conn.close();
				_conn = null;
				_access = null;
			} catch (SQLException e) {
				throw propagate(e);
			}
		}
	}

}
