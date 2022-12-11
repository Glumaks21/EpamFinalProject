package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.db.dao.BookDao;
import ua.maksym.hlushchenko.db.dao.ReaderDao;
import ua.maksym.hlushchenko.db.entity.Book;
import ua.maksym.hlushchenko.db.entity.Subscription;
import ua.maksym.hlushchenko.db.entity.impl.SubscriptionImpl;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.sql.*;
import java.util.*;


public class SubscriptionSqlDao extends AbstractSqlDao<Integer, Subscription> {
    private static final String SQL_SELECT_ALL = "SELECT * FROM subscription";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM subscription " +
            "WHERE id = ?";
    private static final String SQL_INSERT = "INSERT INTO " +
            "subscription(id, reader_login, book_id, taken_date, brought_date, fine) " +
            "VALUE(?, ?, ?, ?, ?, ?)";

    private static final Logger log = LoggerFactory.getLogger(SubscriptionSqlDao.class);

    public SubscriptionSqlDao(Connection connection) {
        super(connection);
    }

    Subscription mapToSubscription(ResultSet resultSet) throws SQLException {
        Subscription subscription = new SubscriptionImpl();
        subscription.setId(resultSet.getInt("id"));

        ReaderDao readerDao = new ReaderSqlDao(connection);
        Reader reader = readerDao.find(resultSet.getString("reader_login")).get();
        subscription.setReader(reader);

        BookDao bookDao = new BookSqlDao(connection);
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

        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Subscription subscription = mapToSubscription(resultSet);
                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return subscriptions;
    }

    @Override
    public Optional<Subscription> find(Integer id) {
        return Optional.empty();
    }

    @Override
    public void save(Subscription entity) {

    }

    @Override
    public void update(Subscription entity) {

    }

    @Override
    public void delete(Integer id) {

    }
}
