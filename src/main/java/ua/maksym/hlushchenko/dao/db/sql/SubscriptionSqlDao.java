package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.SubscriptionImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;


public class SubscriptionSqlDao extends AbstractSqlDao<Integer, Subscription> implements SubscriptionDao {
    private static final String SQL_SELECT_ALL = "SELECT * FROM subscription";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM subscription " +
            "WHERE id = ?";
    private static final String SQL_SELECT_BY_READER_LOGIN = "SELECT * FROM subscription " +
            "WHERE reader_login = ?";
    private static final String SQL_INSERT = "INSERT INTO " +
            "subscription(reader_login, book_id, taken_date, brought_date, fine) " +
            "VALUE(?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE subscription SET " +
            "reader_login = ?, book_id = ?, taken_date = ?, brought_date = ?, fine = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM subscription " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_READER_LOGIN = "DELETE FROM subscription " +
            "WHERE reader_login = ?";


    private static final Logger log = LoggerFactory.getLogger(SubscriptionSqlDao.class);

    public SubscriptionSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    protected Subscription mapToEntity(ResultSet resultSet) throws SQLException {
        Subscription subscription = new SubscriptionImpl();
        subscription.setId(resultSet.getInt("id"));

        ReaderDao readerDao = new ReaderSqlDao(dataSource);
        Reader reader = readerDao.find(resultSet.getString("reader_login")).get();
        subscription.setReader(reader);

        BookDao bookDao = new BookSqlDao(dataSource);
        Book book = bookDao.find(resultSet.getInt("book_id")).get();
        subscription.setBook(book);

        subscription.setTakenDate(resultSet.getDate("taken_date").toLocalDate());
        subscription.setBroughtDate(resultSet.getDate("brought_date").toLocalDate());
        subscription.setFine(resultSet.getDouble("fine"));
        return subscription;
    }

    @Override
    public List<Subscription> findAll() {
        List<Subscription> subscriptions = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Subscription subscription = mapToEntity(resultSet);
                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return subscriptions;
    }

    @Override
    public Optional<Subscription> find(Integer id) {
        Subscription subscription = null;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                subscription = mapToEntity(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(subscription);
    }

    @Override
    public void save(Subscription subscription) {
        dmlOperation(SubscriptionSqlDao::saveInTransaction, subscription);
    }

    @Override
    public void update(Subscription subscription) {
        dmlOperation(SubscriptionSqlDao::updateInTransaction, subscription);
    }

    @Override
    public void delete(Integer id) {
        dmlOperation(SubscriptionSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Subscription subscription, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement,
                subscription.getReader().getLogin(),
                subscription.getBook().getId(),
                Date.valueOf(subscription.getTakenDate()),
                Date.valueOf(subscription.getBroughtDate()),
                subscription.getFine());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        while (resultSet.next()) {
            subscription.setId(resultSet.getInt(1));
        }
    }

    static void updateInTransaction(Subscription subscription, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                subscription.getReader().getLogin(),
                subscription.getBook().getId(),
                subscription.getTakenDate(),
                subscription.getBroughtDate(),
                subscription.getFine(),
                subscription.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public List<Subscription> findByReaderLogin(String login) {
        List<Subscription> subscriptions = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_READER_LOGIN);
            fillPreparedStatement(statement, login);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Subscription subscription = mapToEntity(resultSet);
                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return subscriptions;
    }

    @Override
    public void deleteByReaderLogin(String login) {
        dmlOperation(SubscriptionSqlDao::deleteByReaderLoginInTransaction, login);
    }

    static void deleteByReaderLoginInTransaction(String login, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_READER_LOGIN);
        fillPreparedStatement(statement, login);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
