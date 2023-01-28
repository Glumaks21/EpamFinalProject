package ua.maksym.hlushchenko.orm.dao;

import org.slf4j.*;

import ua.maksym.hlushchenko.orm.exception.DaoException;

import java.sql.*;
import java.util.*;

abstract class AbstractSqlDao<K, T> implements ObjectDao<K, T> {
    private final Session session;

    private static final Logger log = LoggerFactory.getLogger(AbstractSqlDao.class);

    public AbstractSqlDao(Session session) {
        this.session = session;
    }

    protected Session getSession() {
        return session;
    }

    protected abstract T mapEntity(ResultSet resultSet);

    protected Optional<T> querySingle(String sqlQuery, Object... args) {
        try (ResultSet resultSet = session.query(sqlQuery, args)) {
            resultSet.setFetchSize(1);
            List<T> mappedQuery = mapResultSet(this::mapEntity, resultSet);
            return mappedQuery.isEmpty()?
                    Optional.empty():
                    Optional.of(mappedQuery.get(0));
        } catch (SQLException e) {
            log.warn("Failed to query single entity");
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

    protected List<T> queryList(String sqlQuery, Object... args) {
        try (ResultSet resultSet = session.query(sqlQuery, args)) {
            return mapResultSet(this::mapEntity, resultSet);
        } catch (SQLException e) {
            log.warn("Failed to query entity list");
            throw new DaoException(e);
        }
    }
}
