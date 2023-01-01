package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.SubscriptionImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.sql.Date;
import java.util.*;


class SubscriptionSqlDao extends AbstractSqlDao<Integer, Subscription> implements SubscriptionDao {
    private static final String SQL_SELECT_ALL = "SELECT id, reader_id, book_id, taken_date, brought_date, fine " +
            "FROM subscription";
    private static final String SQL_SELECT_BY_ID = "SELECT id, reader_id, book_id, taken_date, brought_date, fine " +
            "FROM subscription " +
            "WHERE id = ?";
    private static final String SQL_SELECT_BY_READER_ID = "SELECT id, reader_id, book_id, taken_date, brought_date, fine " +
            "FROM subscription " +
            "WHERE reader_id = ?";
    private static final String SQL_INSERT = "INSERT INTO " +
            "subscription(reader_id, book_id, taken_date, brought_date, fine) " +
            "VALUE(?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE subscription " +
            "SET reader_id = ?, book_id = ?, taken_date = ?, brought_date = ?, fine = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM subscription " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_READER_ID = "DELETE FROM subscription " +
            "WHERE reader_id = ?";

    private static final Logger log = LoggerFactory.getLogger(SubscriptionSqlDao.class);

    public SubscriptionSqlDao(Connection connection) {
        super(connection);
    }

    protected Subscription mapToEntity(ResultSet resultSet) {
        try {
            Subscription subscription = new SubscriptionImpl();
            subscription.setId(resultSet.getInt("id"));

            ReaderSqlDao readerSqlDao = new ReaderSqlDao(connection);
            Reader reader = readerSqlDao.find(resultSet.getInt("reader_id")).get();
            subscription.setReader(reader);

            BookSqlDao bookSqlDao = new BookEnSqlDao(connection);
            Book book = bookSqlDao.find(resultSet.getInt("book_id")).get();
            subscription.setBook(book);

            subscription.setTakenDate(resultSet.getDate("taken_date").toLocalDate());
            subscription.setBroughtDate(resultSet.getDate("brought_date").toLocalDate());
            subscription.setFine(resultSet.getDouble("fine"));
            return (Subscription) Proxy.newProxyInstance(
                    SubscriptionSqlDao.class.getClassLoader(),
                    new Class[]{Subscription.class, LoadProxy.class},
                    new LoadHandler<>(subscription));
        } catch (SQLException | ConnectionException | NoSuchElementException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }

    @Override
    public List<Subscription> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Subscription> find(Integer id) {
        List<Subscription> subscriptions = mappedQuery(SQL_SELECT_BY_ID, id);
        if (subscriptions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(subscriptions.get(0));
    }

    @Override
    public void save(Subscription subscription) {
        try (ResultSet resultSet = updateQueryWithKeys(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS,
                subscription.getReader().getId(),
                subscription.getBook().getId(),
                Date.valueOf(subscription.getTakenDate()),
                Date.valueOf(subscription.getBroughtDate()),
                subscription.getFine())) {
            while (resultSet.next()) {
                subscription.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Subscription subscription) {
        updateQuery(SQL_UPDATE_BY_ID,
                subscription.getReader().getId(),
                subscription.getBook().getId(),
                subscription.getTakenDate(),
                subscription.getBroughtDate(),
                subscription.getFine(),
                subscription.getId());
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }

    @Override
    public List<Subscription> findByReaderId(Integer id) {
        return mappedQuery(SQL_SELECT_BY_READER_ID, id);
    }

    @Override
    public void deleteByReaderId(Integer id) {
        updateQuery(SQL_DELETE_BY_READER_ID, id);
    }
}
