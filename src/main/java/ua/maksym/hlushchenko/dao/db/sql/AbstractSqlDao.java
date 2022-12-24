package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.sql.*;
import java.util.*;

abstract class AbstractSqlDao<K, T> implements Dao<K, T> {
    protected final Connection connection;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractSqlDao(Connection connection) {
        try {
            if (Objects.requireNonNull(connection).isClosed()) {
                throw new ConnectionException();
            }
        } catch (SQLException e) {
            throw new ConnectionException(e);
        }
        this.connection = connection;
    }

    protected abstract T mapToEntity(ResultSet resultSet);

    protected ResultSet query(String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            fillPreparedStatement(statement, args);
            log.info("Try to execute query:\n" + formatSql(statement));
            return statement.executeQuery();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    protected static void fillPreparedStatement(PreparedStatement statement, Object... args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    protected List<T> mappedQuery(String query, Object... args) {
        return mappedQuery(this::mapToEntity, query, args);
    }

    protected <U> List<U> mappedQuery(SqlMapper<U> mapper, String query, Object... args) {
        return mapResultSet(mapper, query(query, args));
    }

    protected  <U> List<U> mapResultSet(SqlMapper<U> mapper, ResultSet resultSet)  {
        try {
            List<U> entities = new ArrayList<>();
            while (resultSet.next()) {
                U entity = mapper.map(resultSet);
                entities.add(entity);
            }
            return entities;
        } catch (SQLException | MappingException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    protected ResultSet updateQuery(String updateQuery, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(updateQuery,
                    Statement.RETURN_GENERATED_KEYS);
            fillPreparedStatement(statement, args);
            log.info("Try to execute update:\n" + formatSql(statement));
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

    protected static String formatSql(Statement statement) {
        Objects.requireNonNull(statement);
        String dirtySqlQuery = statement.toString();
        return formatSql(dirtySqlQuery.substring(dirtySqlQuery.indexOf(": ") + 1));
    }

    protected static String formatSql(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query is " + sqlQuery);
        }

        StringBuilder strb = new StringBuilder();
        String[] words = sqlQuery.trim().split("\\s+");

        for (int i = 0; i < words.length - 1; i++) {
            strb.append(words[i]);

            String nextWord = words[i + 1];
            if (nextWord.equalsIgnoreCase("JOIN") ||
                    nextWord.equalsIgnoreCase("WHERE")) {
                strb.append("\n");
            } else {
                strb.append(" ");
            }
        }

        strb.append(words[words.length - 1]);
        return strb.toString();
    }
}
