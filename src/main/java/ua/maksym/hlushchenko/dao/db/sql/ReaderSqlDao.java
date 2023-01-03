package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.ReaderDao;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.sql.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.exception.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

class ReaderSqlDao extends UserWithRoleSqlDao<Reader> implements ReaderDao {
    static final String SQL_TABLE_NAME = "reader";
    static final String SQL_COLUMN_NAME_ID = "user_id";
    static final String SQL_COLUMN_NAME_BLOCKED = "blocked";

    private static final String SQL_SELECT_ALL = "SELECT id, login, password_hash, blocked " +
            "FROM reader r " +
            "JOIN user u ON r.user_id = u.id";
    private static final String SQL_SELECT_BY_ID = "SELECT id, login, password_hash, blocked " +
            "FROM reader r " +
            "JOIN user u ON r.user_id = u.id " +
            "WHERE user_id = ?";
    private static final String SQL_SELECT_READER_RECEIPTS = "SELECT id, r.reader_id as reader_id, time " +
            "FROM receipt r " +
            "JOIN reader_has_receipt rhr ON rhr.receipt_id = r.id " +
            "WHERE r.reader_id = ?";
    private static final String SQL_INSERT = "INSERT INTO reader" +
            "(user_id, blocked) " +
            "VALUES(?, ?)";
    private static final String SQL_INSERT_READER_RECEIPT = "INSERT INTO reader_has_receipt" +
            "(reader_id, receipt_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE reader " +
            "SET blocked = ? " +
            "WHERE user_id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM reader " +
            "WHERE user_id = ?";
    private static final String SQL_DELETE_READER_RECEIPTS = "DELETE FROM reader_has_receipt " +
            "WHERE reader_id = ?";

    private static final Logger log = LoggerFactory.getLogger(ReaderSqlDao.class);

    public ReaderSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Reader mapToEntity(ResultSet resultSet) {
        try {
            Reader reader = new ReaderImpl();
            reader.setId(resultSet.getInt("id"));
            reader.setLogin(resultSet.getString("login"));
            reader.setPasswordHash(resultSet.getString("password_hash"));
            reader.setBlocked(resultSet.getBoolean("blocked"));
            return (Reader) Proxy.newProxyInstance(
                    ReaderSqlDao.class.getClassLoader(),
                    new Class[]{Reader.class},
                    new LazyInitializationHandler(reader));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }

    private class LazyInitializationHandler implements InvocationHandler {
        private final Reader wrapped;
        private boolean receiptsInitialised;
        private boolean subscriptionsInitialised;

        public LazyInitializationHandler(Reader wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!receiptsInitialised && method.getName().equals("getReceipts")) {
                receiptsInitialised = true;
                wrapped.setReceipts(findReceipts(wrapped.getId()));
            } else if (!subscriptionsInitialised && method.getName().equals("getSubscriptions")) {
                subscriptionsInitialised = true;
                wrapped.setSubscriptions(findSubscriptions(wrapped.getId()));
            }
            return method.invoke(wrapped, args);
        }
    }

    @Override
    public List<Reader> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Reader> find(Integer id) {
        List<Reader> readers = mappedQuery(SQL_SELECT_BY_ID, id);
        if (readers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(readers.get(0));
    }

    @Override
    public void save(Reader reader) {
        super.save(reader);
        updateQuery(SQL_INSERT, reader.getId(), reader.isBlocked());
        saveReceipts(reader);
        saveSubscriptions(reader);
    }

    @Override
    public void update(Reader reader) {
        super.update(reader);
        updateQuery(SQL_UPDATE_BY_ID,
                reader.isBlocked(),
                reader.getId());
        updateReceipts(reader);
        updateSubscriptions(reader);
    }

    @Override
    public void delete(Integer id) {
        deleteReceipts(id);
        deleteSubscriptions(id);
        updateQuery(SQL_DELETE_BY_ID, id);
        super.delete(id);
    }

    @Override
    public List<Receipt> findReceipts(Integer id) {
        ReceiptSqlDao receiptSqlDao = new ReceiptSqlDao(connection);
        return mappedQuery(receiptSqlDao::mapToEntity, SQL_SELECT_READER_RECEIPTS, id);
    }

    @Override
    public void saveReceipts(Reader reader) {
        ReceiptSqlDao receiptSqlDao = new ReceiptSqlDao(connection);
        for (Receipt receipt : reader.getReceipts()) {
            receipt.setReader(reader);
            receiptSqlDao.save(receipt);

            updateQuery(SQL_INSERT_READER_RECEIPT,
                    reader.getId(),
                    receipt.getId());
        }
    }

    @Override
    public void updateReceipts(Reader reader) {
        deleteReceipts(reader.getId());
        saveReceipts(reader);
    }

    @Override
    public void deleteReceipts(Integer id) {
        updateQuery(SQL_DELETE_READER_RECEIPTS, id);
        ReceiptSqlDao receiptSqlDao = new ReceiptSqlDao(connection);
        receiptSqlDao.deleteByReaderId(id);
    }

    @Override
    public List<Subscription> findSubscriptions(Integer id) {
        SubscriptionSqlDao subscriptionSqlDao = new SubscriptionSqlDao(connection);
        return  subscriptionSqlDao.findByReaderId(id);
    }

    @Override
    public void saveSubscriptions(Reader reader) {
        SubscriptionSqlDao subscriptionSqlDao = new SubscriptionSqlDao(connection);
        for (Subscription subscription : reader.getSubscriptions()) {
            subscription.setReader(reader);
            subscriptionSqlDao.save(subscription);
        }
    }

    @Override
    public void updateSubscriptions(Reader reader) {
        deleteSubscriptions(reader.getId());
        saveSubscriptions(reader);
    }

    @Override
    public void deleteSubscriptions(Integer id) {
        SubscriptionSqlDao subscriptionSqlDao = new SubscriptionSqlDao(connection);
        subscriptionSqlDao.deleteByReaderId(id);
    }
}
