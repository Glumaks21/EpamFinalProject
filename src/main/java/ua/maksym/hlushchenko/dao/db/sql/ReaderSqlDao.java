package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.ReaderDao;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class ReaderSqlDao extends AbstractSqlDao<Integer, Reader> implements ReaderDao<Integer> {
    private static final String SQL_SELECT_ALL = "SELECT id, login, blocked " +
            "FROM reader r " +
            "JOIN user u ON r.used_id = u.login";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT id, login, blocked " +
            "FROM reader r " +
            "JOIN user u ON r.user_id = u.id " +
            "WHERE user_id = ?";
    private static final String SQL_SELECT_READER_RECEIPTS = "SELECT id, reader_id, time " +
            "FROM receipt r " +
            "JOIN reader_has_receipt rhr ON rhr.receipt_id = r.id" +
            "WHERE rhr.reader_id = ?";
    private static final String SQL_INSERT = "INSERT INTO reader" +
            "(user_id, blocked) " +
            "VALUES(?, ?)";
    private static final String SQL_INSERT_READER_RECEIPT = "INSERT INTO reader_has_receipt" +
            "(reader_id, receipt_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_LOGIN = "UPDATE reader " +
            "SET blocked = ? " +
            "WHERE user_id = ?";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM reader " +
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
            reader.setLogin(resultSet.getString("login"));
            reader.setPasswordHash(resultSet.getString("password_hash"));
            reader.setBlocked(resultSet.getBoolean("blocked"));
            return (Reader) Proxy.newProxyInstance(
                    ReaderSqlDao.class.getClassLoader(),
                    new Class[]{Reader.class},
                    new LazyInitializationHandler(reader));
        } catch (SQLException e) {
            throw new MappingException(e);
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
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Reader> find(Integer id) {
        List<Reader> readers = mappedQueryResult(SQL_SELECT_BY_LOGIN, id);
        if (readers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(readers.get(0));
    }

    @Override
    public void save(Reader reader) {
        updateInTransaction(ReaderSqlDao::saveInSession, reader);
    }

    @Override
    public void update(Reader reader) {
        updateInTransaction(ReaderSqlDao::updateInSession, reader);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(ReaderSqlDao::deleteInSession, id);
    }

    static void saveInSession(Reader reader, Connection connection) throws SQLException {
        UserSqlDao.saveInTransaction(reader, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                reader.getId(),
                reader.isBlocked());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        saveReceiptsInTransaction(reader, connection);
        saveSubscriptionsInTransaction(reader, connection);
    }

    static void updateInSession(Reader reader, Connection connection) throws SQLException {
        UserSqlDao.updateInTransaction(reader, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_LOGIN);
        fillPreparedStatement(statement,
                reader.isBlocked(),
                reader.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        updateReceiptsInTransaction(reader, connection);
        updateSubscriptionsInTransaction(reader, connection);
    }

    static void deleteInSession(Integer id, Connection connection) throws SQLException {
        deleteReceiptsInTransaction(id, connection);
        deleteSubscriptionsInTransaction(id, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        UserSqlDao.deleteInTransaction(id, connection);
    }

    @Override
    public List<Receipt> findReceipts(Integer id) {
        try (SqlDaoFactory sqlDaoFactory = new SqlDaoFactory()) {
            ReceiptSqlDao receiptSqlDao = sqlDaoFactory.createReceiptDao();
            return mappedQueryResult(receiptSqlDao::mapToEntity, SQL_SELECT_READER_RECEIPTS, id);
        } catch (ConnectionException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void saveReceipts(Reader reader) {
        updateInTransaction(ReaderSqlDao::saveReceiptsInTransaction, reader);
    }

    @Override
    public void updateReceipts(Reader reader) {
        updateInTransaction(ReaderSqlDao::updateReceiptsInTransaction, reader);
    }

    @Override
    public void deleteReceipts(Integer id) {
        updateInTransaction(ReaderSqlDao::deleteReceiptsInTransaction, id);
    }

    static void saveReceiptsInTransaction(Reader reader, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_READER_RECEIPT);
        for (Receipt receipt : reader.getReceipts()) {
            receipt.setReader(reader);
            ReceiptSqlDao.saveInTransaction(receipt, connection);

            fillPreparedStatement(statement, reader.getId(), receipt.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();
        }
    }

    static void updateReceiptsInTransaction(Reader reader, Connection connection) throws SQLException {
        deleteReceiptsInTransaction(reader.getId(), connection);
        saveReceiptsInTransaction(reader, connection);
    }

    static void deleteReceiptsInTransaction(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_READER_RECEIPTS);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ReceiptSqlDao.deleteInTransaction(id, connection);
    }

    @Override
    public List<Subscription> findSubscriptions(Integer id) {
        SubscriptionSqlDao subscriptionSqlDao = new SubscriptionSqlDao(connection);
        return  subscriptionSqlDao.findByReaderId(id);
    }

    @Override
    public void saveSubscriptions(Reader reader) {
        updateInTransaction(ReaderSqlDao::saveSubscriptionsInTransaction, reader);
    }

    @Override
    public void updateSubscriptions(Reader reader) {
        updateInTransaction(ReaderSqlDao::updateSubscriptionsInTransaction, reader);
    }

    @Override
    public void deleteSubscriptions(Integer id) {
        updateInTransaction(ReaderSqlDao::deleteSubscriptionsInTransaction, id);
    }

    static void saveSubscriptionsInTransaction(Reader reader, Connection connection) throws SQLException {
        for (Subscription subscription : reader.getSubscriptions()) {
            subscription.setReader(reader);
            SubscriptionSqlDao.saveInTransaction(subscription, connection);
        }
    }

    static void updateSubscriptionsInTransaction(Reader reader, Connection connection) throws SQLException {
        deleteSubscriptionsInTransaction(reader.getId(), connection);
        saveSubscriptionsInTransaction(reader, connection);
    }

    static void deleteSubscriptionsInTransaction(Integer id, Connection connection) throws SQLException {
         SubscriptionSqlDao.deleteByReaderIdInTransaction(id, connection);
    }
}
