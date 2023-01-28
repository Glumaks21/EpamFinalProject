package ua.maksym.hlushchenko.orm.dao;

import lombok.extern.slf4j.Slf4j;
import ua.maksym.hlushchenko.dao.db.sql.SqlQueryFormatter;
import ua.maksym.hlushchenko.orm.exception.SessionException;

import java.sql.*;
import java.util.Arrays;

@Slf4j
public class SessionImpl implements Session {
    private final Connection connection;

    public SessionImpl(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalArgumentException("Connection: " + connection);
            }
            connection.setAutoCommit(false);
            this.connection = connection;
        } catch (SQLException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void command(String query) {
        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute command:\n" + query);
            statement.execute(query);
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new SessionException("Failed to execute command: " + query, e);
        }
    }

    @Override
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

    @Override
    public void update(String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            fillPreparedStatement(statement, args);
            log.info("Try to execute update:\n" + SqlQueryFormatter.formatSql(statement));
            statement.executeUpdate();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
            throw new SessionException("Failed to execute query: " + query +
                    (args.length != 0? " with args " + Arrays.toString(args): ""), e);
        }
    }

    @Override
    public ResultSet updateWithKeys(String query, Object... args) {
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
                    (args.length != 0? " with args " + Arrays.toString(args): ""), e);
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

    @Override
    public void commit() {
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            throw new SessionException(e);
        }
    }
}
