package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.PublisherDao;
import ua.maksym.hlushchenko.dao.entity.Publisher;
import ua.maksym.hlushchenko.dao.entity.sql.PublisherImpl;
import ua.maksym.hlushchenko.exception.*;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

class PublisherSqlDao extends AbstractSqlDao<Integer, Publisher> implements PublisherDao {
    private static final String SQL_TABLE_NAME = "publisher";
    private static final String SQL_COLUMN_NAME_ID = "id";
    private static final String SQL_COLUMN_NAME_NAME = "name";

    private static final String SQL_SELECT_ALL = QueryUtil.createSelect(
            SQL_TABLE_NAME,  SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME);

    private static final String SQL_SELECT_BY_ID = QueryUtil.createSelectWithConditions(
            SQL_TABLE_NAME, List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME), List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_SELECT_BY_NAME = QueryUtil.createSelectWithConditions(
            SQL_TABLE_NAME, List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME), List.of(SQL_COLUMN_NAME_NAME));

    private static final String SQL_UPDATE_BY_ID = QueryUtil.createUpdate(
            SQL_TABLE_NAME, List.of(SQL_COLUMN_NAME_NAME), List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_INSERT = QueryUtil.createInsert(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_NAME);

    private static final String SQL_DELETE_BY_ID = QueryUtil.createDelete
            (SQL_TABLE_NAME, SQL_COLUMN_NAME_ID);

    private static final String SQL_DELETE_BY_NAME = QueryUtil.createDelete
            (SQL_TABLE_NAME, SQL_COLUMN_NAME_NAME);

    private static final Logger log = LoggerFactory.getLogger(PublisherSqlDao.class);

    public PublisherSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Publisher mapToEntity(ResultSet resultSet) {
        try {
            Publisher publisher = new PublisherImpl();
            publisher.setId(resultSet.getInt(SQL_COLUMN_NAME_ID));
            publisher.setName(resultSet.getString(SQL_COLUMN_NAME_NAME));
            return (Publisher) Proxy.newProxyInstance(
                    PublisherSqlDao.class.getClassLoader(),
                    new Class[]{Publisher.class, LoadProxy.class},
                    new LoadHandler<>(publisher));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }

    @Override
    public List<Publisher> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Publisher> find(Integer id) {
        List<Publisher> publishers = mappedQuery(SQL_SELECT_BY_ID, id);
        if (publishers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(publishers.get(0));
    }

    @Override
    public void save(Publisher publisher) {
        try (ResultSet resultSet = updateQueryWithKeys(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS, publisher.getName())) {
            if (resultSet.next()) {
                publisher.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Publisher publisher) {
        updateQuery(SQL_UPDATE_BY_ID, publisher.getName(), publisher.getId());
    }


    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);;
    }

    @Override
    public Optional<Publisher> findByName(String name) {
        List<Publisher> publishers = mappedQuery(SQL_SELECT_BY_NAME, name);
        if (publishers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(publishers.get(0));
    }

    @Override
    public void deleteByName(String name) {
        updateQuery(SQL_DELETE_BY_NAME, name);;
    }
}
