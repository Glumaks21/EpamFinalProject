package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.exception.SessionException;

import java.sql.*;
import java.util.Arrays;

public class Session {
    private final Connection connection;

    private static final Logger log = LoggerFactory.getLogger(Session.class);

    public Session(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalArgumentException();
            }
            connection.setAutoCommit(false);
            this.connection = connection;
        } catch (SQLException e) {
            throw new IllegalArgumentException();
        }
    }

    public ResultSet query(String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            fillPreparedStatement(statement, args);
            log.info("Try to execute query:\n" + SqlQueryFormatter.formatSql(statement));
            return statement.executeQuery();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new SessionException("Failed to execute query: " + query +
                    " with args " + Arrays.toString(args),e);
        }
    }

    private static void fillPreparedStatement(PreparedStatement statement, Object... args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    public void updateQuery(String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            fillPreparedStatement(statement, args);
            log.info("Try to execute update:\n" + SqlQueryFormatter.formatSql(statement));
            statement.executeUpdate();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
            throw new SessionException("Failed to execute query: " + query +
                    " with args " + Arrays.toString(args),e);
        }
    }

    protected ResultSet updateQueryWithKeys(String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            fillPreparedStatement(statement, args);
            log.info("Try to execute update:\n" + SqlQueryFormatter.formatSql(statement));
            statement.executeUpdate();
            return statement.getGeneratedKeys();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
            throw new SessionException("Failed to execute update: " + query +
                    " with args " + Arrays.toString(args),e);
        }
    }

    private void tryToRollBack() {
        try {
            log.info("Try to roll back: " + connection);
            if (!connection.getAutoCommit()) {
                connection.rollback();
                log.info("Roll back successful");
            }
        } catch (SQLException e) {
            log.warn("Roll back failed: " + e.getMessage());
            throw new SessionException(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new SessionException(e);
        }
    }

    public void closeSession() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new SessionException(e);
        }
    }
}
