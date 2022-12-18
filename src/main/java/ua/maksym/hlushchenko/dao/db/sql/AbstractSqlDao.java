package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.exception.DaoException;

import java.sql.*;
import java.util.*;

public abstract class AbstractSqlDao<K, T> implements Dao<K, T> {
    protected final Connection connection;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractSqlDao(Connection Connection) {
        Objects.requireNonNull(Connection);
        this.connection = Connection;
    }

    protected abstract T mapToEntity(ResultSet resultSet);

    protected List<T> mappedQueryResult(String query, Object... args) {
        return mappedQueryResult(this::mapToEntity, query, args);
    }

    protected <U> List<U> mappedQueryResult(SqlMapper<U> mapper, String query, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            fillPreparedStatement(statement, args);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            return mapFromResultSet(mapper, resultSet);
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    private <U> List<U> mapFromResultSet(SqlMapper<U> mapper, ResultSet resultSet) throws SQLException {
        List<U> entities = new ArrayList<>();
        while (resultSet.next()) {
            U entity = mapper.map(resultSet);
            entities.add(entity);
        }
        return entities;
    }

    protected <U> void updateInTransaction(SqlBiConsumer<U> methodWithQueriesInTransaction, U initArg) {
        try {
            connection.setAutoCommit(false);

            methodWithQueriesInTransaction.accept(initArg, connection);

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
            throw new DaoException(e);
        }
    }

    private void tryToRollBack() {
        log.info("Try to roll back: " + connection);
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.warn("Roll back failed: " + e.getMessage());
            throw new DaoException(e);
        }
        log.info("Roll back successful");
    }

    protected static void fillPreparedStatement(PreparedStatement statement, Object... args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
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
