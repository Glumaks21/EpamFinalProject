package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.ReaderDao;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import javax.sql.DataSource;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class ReaderSqlDao extends AbstractSqlDao<String, Reader> implements ReaderDao {
    private static final String SQL_SELECT_ALL = "SELECT * FROM reader r " +
            "JOIN user u ON r.user_login = u.login";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM reader r " +
            "JOIN user u ON r.user_login = u.login " +
            "WHERE login = ?";
    private static final String SQL_SELECT_READER_RECEIPTS = "SELECT * FROM receipt r " +
            "JOIN reader_has_receipt rhr ON r.id = rhr.receipt_id " +
            "WHERE rhr.reader_login = ?";
    private static final String SQL_INSERT = "INSERT INTO reader(user_login, blocked) " +
            "VALUES(?, ?)";
    private static final String SQL_INSERT_READER_RECEIPT = "INSERT INTO reader_has_receipt(reader_login, receipt_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_LOGIN = "UPDATE reader SET " +
            "blocked = ? " +
            "WHERE user_login = ?";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM reader " +
            "WHERE user_login = ?";
    private static final String SQL_DELETE_READER_RECEIPTS = "DELETE FROM reader_has_receipt " +
            "WHERE reader_login = ?";


    private static final Logger log = LoggerFactory.getLogger(ReaderSqlDao.class);

    public ReaderSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Reader mapToEntity(ResultSet resultSet) throws SQLException {
        ReaderImpl reader = new ReaderImpl();
        reader.setLogin(resultSet.getString("login"));
        reader.setPassword(resultSet.getString("password"));
        reader.setBlocked(resultSet.getBoolean("blocked"));
        return (Reader) Proxy.newProxyInstance(
                ReaderSqlDao.class.getClassLoader(),
                new Class[]{Reader.class},
                new LazyInitializationHandler(reader));
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
                wrapped.setReceipts(findReceipts(wrapped.getLogin()));
            } else if (!subscriptionsInitialised && method.getName().equals("getSubscriptions")) {
                subscriptionsInitialised = true;
                wrapped.setSubscriptions(findSubscriptions(wrapped.getLogin()));
            }
            return method.invoke(wrapped, args);
        }
    }

    @Override
    public List<Reader> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Reader> find(String login) {
        List<Reader> readers = mappedQueryResult(SQL_SELECT_BY_LOGIN, login);
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
    public void delete(String login) {
        updateInTransaction(ReaderSqlDao::deleteInSession, login);
    }

    static void saveInSession(Reader reader, Connection connection) throws SQLException {
        UserSqlDao.saveInTransaction(reader, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                reader.getLogin(),
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
                reader.getLogin());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        updateReceiptsInTransaction(reader, connection);
        updateSubscriptionsInTransaction(reader, connection);
    }

    static void deleteInSession(String login, Connection connection) throws SQLException {
        deleteReceiptsInTransaction(login, connection);
        deleteSubscriptionsInTransaction(login, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, login);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        UserSqlDao.deleteInTransaction(login, connection);
    }

    @Override
    public List<Receipt> findReceipts(String login) {
        ReceiptSqlDao receiptSqlDao = new ReceiptSqlDao(dataSource);
        return mappedQueryResult(receiptSqlDao::mapToEntity, SQL_SELECT_READER_RECEIPTS, login);
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
    public void deleteReceipts(String login) {
        updateInTransaction(ReaderSqlDao::deleteReceiptsInTransaction, login);
    }

    static void saveReceiptsInTransaction(Reader reader, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_READER_RECEIPT);
        for (Receipt receipt : reader.getReceipts()) {
            fillPreparedStatement(statement, reader.getLogin(), receipt.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();
        }
    }

    static void updateReceiptsInTransaction(Reader reader, Connection connection) throws SQLException {
        deleteReceiptsInTransaction(reader.getLogin(), connection);
        saveReceiptsInTransaction(reader, connection);
    }

    static void deleteReceiptsInTransaction(String login, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_READER_RECEIPTS);
        fillPreparedStatement(statement, login);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public List<Subscription> findSubscriptions(String login) {
        SubscriptionSqlDao subscriptionSqlDao = new SubscriptionSqlDao(dataSource);
        return  subscriptionSqlDao.findByReaderLogin(login);
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
    public void deleteSubscriptions(String login) {
        updateInTransaction(ReaderSqlDao::deleteSubscriptionsInTransaction, login);
    }

    static void saveSubscriptionsInTransaction(Reader reader, Connection connection) throws SQLException {
        for (Subscription subscription : reader.getSubscriptions()) {
            SubscriptionSqlDao.saveInTransaction(subscription, connection);
        }
    }

    static void updateSubscriptionsInTransaction(Reader reader, Connection connection) throws SQLException {
        deleteSubscriptionsInTransaction(reader.getLogin(), connection);
        saveSubscriptionsInTransaction(reader, connection);
    }

    static void deleteSubscriptionsInTransaction(String login, Connection connection) throws SQLException {
         SubscriptionSqlDao.deleteByReaderLoginInTransaction(login, connection);
    }
}
