package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.PublisherDao;
import ua.maksym.hlushchenko.dao.entity.Publisher;
import ua.maksym.hlushchenko.dao.entity.impl.PublisherImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class PublisherSqlDao extends AbstractSqlDao<String, Publisher> implements PublisherDao<String> {
    static final String SQL_SELECT_ALL = "SELECT isbn, name " +
            "FROM publisher";
    static final String SQL_SELECT_BY_ISBN = "SELECT isbn, name " +
            "FROM publisher " +
            "WHERE isbn = ?";
    static final String SQL_UPDATE_BY_ISBN = "UPDATE publisher " +
            "SET name = ? " +
            "WHERE isbn = ?";
    static final String SQL_SELECT_BY_NAME = "SELECT isbn, name " +
            "FROM publisher " +
            "WHERE name = ?";
    static final String SQL_INSERT = "INSERT INTO publisher(isbn, name) " +
            "VALUES(?, ?)";
    static final String SQL_DELETE_BY_ISBN = "DELETE FROM publisher " +
            "WHERE isbn = ?";
    static final String SQL_DELETE_BY_NAME = "DELETE FROM publisher " +
            "WHERE name = ?";

    private static final Logger log = LoggerFactory.getLogger(PublisherSqlDao.class);

    public PublisherSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Publisher mapToEntity(ResultSet resultSet) {
        try {
            Publisher publisher = new PublisherImpl();
            publisher.setIsbn(resultSet.getString("isbn"));
            publisher.setName(resultSet.getString("name"));
            return (Publisher) Proxy.newProxyInstance(
                    PublisherSqlDao.class.getClassLoader(),
                    new Class[]{Publisher.class, LoadProxy.class},
                    new LoadHandler<>(publisher));
        } catch (SQLException e) {
            throw new MappingException(e);
        }
    }

    @Override
    public List<Publisher> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Publisher> find(String isbn) {
        List<Publisher> publishers = mappedQuery(SQL_SELECT_BY_ISBN, isbn);
        if (publishers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(publishers.get(0));
    }

    @Override
    public void save(Publisher publisher) {
        updateQuery(SQL_INSERT, publisher.getIsbn(), publisher.getName());
    }

    @Override
    public void update(Publisher publisher) {
        updateQuery(SQL_UPDATE_BY_ISBN, publisher.getName(), publisher.getIsbn());
    }


    @Override
    public void delete(String isbn) {
        updateQuery(SQL_DELETE_BY_ISBN, isbn);;
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
