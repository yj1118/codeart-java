package apros.codeart.ddd.repository.access;

import static apros.codeart.runtime.Util.propagate;

import java.sql.Connection;
import java.sql.SQLException;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.repository.TransactionStatus;
import apros.codeart.i18n.Language;

public class DataConnection implements AutoCloseable {

    private Connection _conn;

    private boolean _isCommitted;

    public boolean isCommitted() {
        return _isCommitted;
    }

    private boolean _tranOpened;

    private DataAccess _access;

    public DataAccess access() {
        this.initialize();
        return _access;
    }

    public DataConnection() {

    }

//	region 初始化

    private void initialize() {
        if (_conn != null) return;

        _conn = createConnection();
        _tranOpened = false;
        _access = new DataAccess(_conn);
        _isCommitted = false;
    }

    public void begin(TransactionStatus status) {
        this.initialize();

        if (_tranOpened)
            throw new DomainDrivenException(Language.strings("apros.codeart.ddd", "TransactionStarted"));
        openTransaction(_conn, status);
        _tranOpened = true;
    }

    private static Connection createConnection() {
        return DataSource.getConnection();
    }

    private static void openTransaction(Connection conn, TransactionStatus status) {
        // 事务的开始是隐式的，从你获取连接并关闭自动提交模式（
        // 通过setAutoCommit(false)）的那一刻开始。这个操作告诉数据库管理系统，
        // 接下来执行的一系列SQL语句应该被视为一个单独的事务，直到你显式地调用commit或rollback方法
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

//            switch (status){
//                case None ->  conn.setTransactionIsolation(Connection.TRANSACTION_NONE);
//                case Share ->  conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//                case Delay, Timely -> conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//            }
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    public void rollback() {
        if (_tranOpened) {
            if (_isCommitted) return; // 已提交，没法回滚了
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
                _isCommitted = true;
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
                _isCommitted = false;
            } catch (SQLException e) {
                throw propagate(e);
            }
        }
    }

}
