package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.exception.*;

import java.sql.*;
import java.util.*;

abstract class AbstractSqlDao<K, T> implements Dao<K, T> {
    protected final Connection connection;

    private static final Logger log = LoggerFactory.getLogger(AbstractSqlDao.class);

    public AbstractSqlDao(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalArgumentException();
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException();
        }

        this.connection = connection;
    }

    protected abstract T mapEntity(ResultSet resultSet);

    private ResultSet query(String query, Object... args) {
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

    protected static void fillPreparedStatement(PreparedStatement statement, Object... args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    protected Optional<T> querySingle(String sqlQuery, Object... args) {
        try (ResultSet resultSet = query(sqlQuery, args)) {
            resultSet.setFetchSize(1);
            List<T> mappedQuery = mapResultSet(this::mapEntity, resultSet);
            return mappedQuery.isEmpty()?
                    Optional.empty():
                    Optional.of(mappedQuery.get(0));
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    protected List<T> queryList(String sqlQuery, Object... args) {
        try (ResultSet resultSet = query(sqlQuery, args)) {
            return mapResultSet(this::mapEntity, resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    protected  <U> List<U> mapResultSet(SqlMapper<U> mapper, ResultSet resultSet)  {
        try {
            List<U> entities = new ArrayList<>();
            while (resultSet.next()) {
                U entity = mapper.map(resultSet);
                entities.add(entity);
            }
            return entities;
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    protected void updateQuery(String query, Object... args) {
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
