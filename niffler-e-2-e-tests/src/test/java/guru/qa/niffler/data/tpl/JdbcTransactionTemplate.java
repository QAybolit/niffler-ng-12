package guru.qa.niffler.data.tpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class JdbcTransactionTemplate {

    private final JdbcConnectionHolder connectionHolder;
    private final AtomicBoolean closeAfterAction = new AtomicBoolean(true);

    public JdbcTransactionTemplate(String jdbcUrl) {
        this.connectionHolder = Connections.holder(jdbcUrl);
    }

    public JdbcTransactionTemplate holdConnectionAfterAction() {
        this.closeAfterAction.set(false);
        return this;
    }

    public <T> T execute(Supplier<T> action, int isolationLevel) {
        Connection connection = null;
        try {
            connection = this.connectionHolder.connection();
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);

            T result = action.get();
            connection.commit();
            connection.setAutoCommit(true);
            return result;
        } catch (Exception | AssertionError e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (closeAfterAction.get()) {
                connectionHolder.close();
            }
        }
    }

    public <T> T execute(Supplier<T> action) {
        return execute(action, Connection.TRANSACTION_READ_COMMITTED);
    }

    public void execute(Runnable action, int isolationLevel) {
        Connection connection = null;
        try {
            connection = this.connectionHolder.connection();
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);

            action.run();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception | AssertionError e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (closeAfterAction.get()) {
                connectionHolder.close();
            }
        }
    }

    public void execute(Runnable action) {
        execute(action, Connection.TRANSACTION_READ_COMMITTED);
    }
}
