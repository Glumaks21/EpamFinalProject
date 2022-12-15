package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.PublisherDao;
import ua.maksym.hlushchenko.dao.entity.Publisher;
import ua.maksym.hlushchenko.dao.entity.impl.PublisherImpl;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class PublisherSqlDao extends AbstractSqlDao<String, Publisher> implements PublisherDao {
    private static final String SQL_SELECT_ALL = "SELECT * FROM publisher";
    private static final String SQL_SELECT_BY_ISBN = "SELECT * FROM publisher " +
            "WHERE isbn = ?";
    private static final String SQL_SELECT_BY_NAME = "SELECT * FROM publisher " +
            "WHERE name = ?";
    private static final String SQL_INSERT = "INSERT INTO publisher(isbn, name) " +
            "VALUES(?, ?)";
    private static final String SQL_DELETE_BY_ISBN = "DELETE FROM publisher " +
            "WHERE isbn = ?";
    private static final String SQL_DELETE_BY_NAME = "DELETE FROM publisher " +
            "WHERE name = ?";

    private static final Logger log = LoggerFactory.getLogger(PublisherSqlDao.class);

    public PublisherSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected PublisherImpl mapToEntity(ResultSet resultSet) throws SQLException {
        PublisherImpl publisher = new PublisherImpl();
        publisher.setIsbn(resultSet.getString("isbn"));
        publisher.setName(resultSet.getString("name"));
        return publisher;
    }

    @Override
    public List<Publisher> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Publisher> find(String isbn) {
        List<Publisher> publishers = mappedQueryResult(SQL_SELECT_BY_ISBN, isbn);
        if (publishers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(publishers.get(0));
    }

    @Override
    public void save(Publisher publisher) {
        updateInTransaction(PublisherSqlDao::saveInSession, publisher);
    }

    @Override
    public void update(Publisher publisher) {}


    @Override
    public void delete(String isbn) {
        updateInTransaction(PublisherSqlDao::deleteByIsbnInTransaction, isbn);
    }

    static void saveInSession(Publisher publisher, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement, publisher.getIsbn(), publisher.getName());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteByIsbnInTransaction(String isbn, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ISBN);
        fillPreparedStatement(statement, isbn);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public Optional<Publisher> findByName(String name) {
        List<Publisher> publishers = mappedQueryResult(SQL_SELECT_BY_NAME, name);
        if (publishers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(publishers.get(0));
    }

    @Override
    public void deleteByName(String name) {
        updateInTransaction(PublisherSqlDao::deleteByNameInTransaction, name);
    }

    static void deleteByNameInTransaction(String name, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_NAME);
        fillPreparedStatement(statement, name);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
