package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.db.dao.PublisherDao;
import ua.maksym.hlushchenko.db.entity.Publisher;
import ua.maksym.hlushchenko.db.entity.model.PublisherModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PublisherSqlDao extends AbstractSqlDao<String, Publisher> implements PublisherDao {
    static String SQL_SELECT_ALL = "SELECT * FROM publisher";
    static String SQL_SELECT_BY_ISBN = "SELECT * FROM publisher " +
            "WHERE isbn = ?";
    static String SQL_SELECT_BY_NAME = "SELECT * FROM publisher " +
            "WHERE name = ?";
    static String SQL_INSERT = "INSERT INTO publisher(isbn, name) " +
            "VALUES(?, ?)";
    static String SQL_DELETE_BY_ISBN = "DELETE FROM publisher " +
            "WHERE isbn = ?";
    static String SQL_DELETE_BY_NAME = "DELETE FROM publisher " +
            "WHERE name = ?";

    private static final Logger log = LoggerFactory.getLogger(PublisherSqlDao.class);

    public PublisherSqlDao(Connection connection) {
        super(connection);
    }

    static PublisherModel mapToPublisher(ResultSet resultSet) throws SQLException {
        PublisherModel publisher = new PublisherModel();
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

    private Optional<Publisher> findPattern(String sqlStatement, String column) {
        Publisher publisher = null;

        try {
            PreparedStatement statement = connection.prepareStatement(sqlStatement);
            fillPreparedStatement(statement, column);

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
    public Optional<Publisher> find(String isbn) {
        return findPattern(SQL_SELECT_BY_ISBN, isbn);
    }

    @Override
    public Optional<Publisher> findByName(String name) {
        return findPattern(SQL_SELECT_BY_NAME, name);
    }

    @Override
    public void save(Publisher publisher) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement, publisher.getIsbn(), publisher.getName());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Publisher publisher) {}

    private void deletePattern(String sqlStatement, String column) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(sqlStatement);
            fillPreparedStatement(statement, column);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }


    @Override
    public void delete(String isbn) {
        deletePattern(SQL_DELETE_BY_ISBN, isbn);
    }

    @Override
    public void deleteByName(String name) {
        deletePattern(SQL_DELETE_BY_NAME, name);
    }
}
