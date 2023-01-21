package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.exception.DaoException;

import java.sql.*;

public class SqlQueryHelper {
    private final Connection connection;

    private static final Logger log = LoggerFactory.getLogger(SqlQueryHelper.class);

    public SqlQueryHelper(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalArgumentException();
            }
            this.connection = connection;
        } catch (SQLException e) {
            throw new IllegalArgumentException();
        }
    }

    public ResultSet query(String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            fillPreparedStatement(statement, args);
            log.info("Try to execute query:\n" + TemplateSqlQueryUtil.formatSql(statement));
            return statement.executeQuery();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException("Failure to request a query: " + query, e);
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
            log.info("Try to execute update:\n" + TemplateSqlQueryUtil.formatSql(statement));
            statement.executeUpdate();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
            throw new DaoException(e);
        }
    }

    protected ResultSet updateQueryWithKeys(String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            fillPreparedStatement(statement, args);
            log.info("Try to execute update:\n" + TemplateSqlQueryUtil.formatSql(statement));
            statement.executeUpdate();
            return statement.getGeneratedKeys();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
            throw new DaoException(e);
        }
    }

    private void tryToRollBack() {
        try {
            log.info("Try to roll back: " + connection);
            if (!connection.getAutoCommit()) {
                connection.rollback();
                log.info("Roll back successful");
            } else {
                log.warn("Autocommit is turned on");
            }
        } catch (SQLException e) {
            log.warn("Roll back failed: " + e.getMessage());
            throw new DaoException(e);
        }
    }
}
