package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.db.dao.PublisherDao;
import ua.maksym.hlushchenko.db.entity.Publisher;
import ua.maksym.hlushchenko.db.entity.impl.PublisherImpl;

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

    public PublisherSqlDao(Connection connection) {
        super(connection);
    }

    private PublisherImpl mapToPublisher(ResultSet resultSet) throws SQLException {
        PublisherImpl publisher = new PublisherImpl();
        publisher.setIsbn(resultSet.getString("isbn"));
        publisher.setName(resultSet.getString("name"));
        return publisher;
    }

    @Override
    public List<Publisher> findAll() {
        List<Publisher> publishers = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                Publisher publisher = mapToPublisher(resultSet);
                publishers.add(publisher);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return publishers;
    }

    @Override
    public Optional<Publisher> find(String isbn) {
        Publisher publisher = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ISBN);
            fillPreparedStatement(statement, isbn);

            log.info("Try to execute:\n" + formatSql(statement));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                publisher = mapToPublisher(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(publisher);
    }

    @Override
    public void save(Publisher publisher) {
        try {
            connection.setAutoCommit(false);
            saveInSession(publisher, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Publisher publisher) {}


    @Override
    public void delete(String isbn) {
        try {
            connection.setAutoCommit(false);
            deleteInSession(isbn, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveInSession(Publisher publisher, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement, publisher.getIsbn(), publisher.getName());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public Optional<Publisher> findByName(String name) {
        Publisher publisher = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_NAME);
            fillPreparedStatement(statement, name);

            log.info("Try to execute:\n" + formatSql(statement));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                publisher = mapToPublisher(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(publisher);
    }

    @Override
    public void deleteByName(String name) {
        try {
            connection.setAutoCommit(false);
            deleteInSessionByName(name, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void deleteInSession(String isbn, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ISBN);
        fillPreparedStatement(statement, isbn);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInSessionByName(String name, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_NAME);
        fillPreparedStatement(statement, name);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
