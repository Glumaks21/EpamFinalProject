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


public class SubscriptionSqlDao extends AbstractSqlDao<Integer, Subscription> implements SubscriptionDao<Integer> {
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

        DaoFactory daoFactory = new SqlDaoFactory();

        Dao<Integer, Reader> readerDao = daoFactory.createReaderDao();
        Reader reader = readerDao.find(resultSet.getInt("reader_id")).get();
        subscription.setReader(reader);

        Dao<Integer, Book> bookDao = daoFactory.createBookDao(Locale.ENGLISH);
        Book book = bookDao.find(resultSet.getInt("book_id")).get();
        subscription.setBook(book);

        subscription.setTakenDate(resultSet.getDate("taken_date").toLocalDate());
        subscription.setBroughtDate(resultSet.getDate("brought_date").toLocalDate());
        subscription.setFine(resultSet.getDouble("fine"));
        return subscription;
    }

    @Override
    public List<Subscription> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Subscription> find(Integer id) {
        List<Subscription> subscriptions = mappedQueryResult(SQL_SELECT_ALL);
        if (subscriptions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(subscriptions.get(0));
    }

    @Override
    public void save(Subscription subscription) {
        updateInTransaction(SubscriptionSqlDao::saveInTransaction, subscription);
    }

    @Override
    public void update(Subscription subscription) {
        updateInTransaction(SubscriptionSqlDao::updateInTransaction, subscription);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(SubscriptionSqlDao::deleteInTransaction, id);
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
    public List<Subscription> findByReaderId(Integer id) {
        return mappedQueryResult(SQL_SELECT_BY_READER_LOGIN, id);
    }

    @Override
    public void deleteByReaderId(Integer id) {
        updateInTransaction(SubscriptionSqlDao::deleteByReaderIdInTransaction, id);
    }

    static void deleteByReaderIdInTransaction(Integer login, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_READER_LOGIN);
        fillPreparedStatement(statement, login);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
