package ua.maksym.hlushchenko.db.dao.sql;


import org.slf4j.*;

import ua.maksym.hlushchenko.db.dao.ReaderDao;
import ua.maksym.hlushchenko.db.entity.*;
import ua.maksym.hlushchenko.db.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.lang.reflect.Proxy;
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
    private static final String SQL_SELECT_READER_SUBSCRIPTIONS = "SELECT * FROM subscription " +
            "WHERE reader_login = ?";
    private static final String SQL_INSERT = "INSERT INTO reader(user_login, blocked) " +
            "VALUES(?, ?)";
    private static final String SQL_INSERT_READER_RECEIPT = "INSERT INTO reader_has_receipt(reader_login, receipt_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_LOGIN = "UPDATE reader SET " +
            "blocked = ? " +
            "WHERE user_login = ?";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM reader " +
            "WHERE user_login = ?";
    private static final String SQL_DELETE_RECEIPTS_BY_READER_LOGIN = "DELETE FROM reader_has_receipt rhr " +
            "WHERE rhr.reader_login = ?";

    private static final Logger log = LoggerFactory.getLogger(ReaderSqlDao.class);

    public ReaderSqlDao(Connection connection) {
        super(connection);
    }

    Reader mapToReader(ResultSet resultSet) throws SQLException {
        ReaderImpl reader = new ReaderImpl();
        reader.setBlocked(resultSet.getBoolean("blocked"));
        reader.setLogin(resultSet.getString("login"));
        reader.setPassword(resultSet.getString("password"));
        return (Reader) Proxy.newProxyInstance(
                ReaderSqlDao.class.getClassLoader(),
                new Class[]{Reader.class},
                (proxy, method, methodArgs) -> {
                    if (method.getName().equals("getReceipts") &&
                            reader.getReceipts() == null) {
                        reader.setReceipts(findReceipts(reader.getLogin()));
                    } else if (method.getName().equals("getSubscriptions") &&
                            reader.getSubscriptions() == null) {
                        reader.setSubscriptions(findSubscriptions(reader.getLogin()));
                    }

                    return method.invoke(reader, methodArgs);
                });
    }

    @Override
    public List<Reader> findAll() {
        List<Reader> readers = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                Reader reader = mapToReader(resultSet);
                readers.add(reader);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return readers;
    }

    @Override
    public Optional<Reader> find(String id) {
        Reader reader = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);

            log.info("Try to execute:\n" + formatSql(statement));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                reader = mapToReader(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(reader);
    }

    @Override
    public void save(Reader reader) {
        try {
            connection.setAutoCommit(false);
            saveInSession(reader, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Reader reader) {
        try {
            connection.setAutoCommit(false);
            updateInSession(reader, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void delete(String login) {
        try {
            connection.setAutoCommit(false);
            deleteInSession(login, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveInSession(Reader reader, Connection connection) throws SQLException {
        UserSqlDao.saveInSession(reader, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                reader.getLogin(),
                reader.isBlocked());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        saveReceiptsInSession(reader, connection);
        saveSubscriptionsInSession(reader, connection);
    }

    static void updateInSession(Reader reader, Connection connection) throws SQLException {
        UserSqlDao.updateInSession(reader, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_LOGIN);
        fillPreparedStatement(statement,
                reader.isBlocked(),
                reader.getLogin());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        updateReceiptsInSession(reader, connection);
        updateSubscriptionsInSession(reader, connection);
    }

    static void deleteInSession(String login, Connection connection) throws SQLException {
        deleteReceiptsInSession(login, connection);
        deleteSubscriptionsInSession(login, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, login);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        UserSqlDao.deleteInSession(login, connection);
    }

    @Override
    public List<Receipt> findReceipts(String login) {
        List<Receipt> receipts = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_READER_RECEIPTS);
            fillPreparedStatement(statement, login);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            ReceiptSqlDao receiptSqlDao = new ReceiptSqlDao(connection);
            while (resultSet.next()) {
                Receipt receipt = receiptSqlDao.mapToReceipt(resultSet);
                receipts.add(receipt);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return receipts;
    }

    @Override
    public void saveReceipts(Reader reader) {
        try {
            connection.setAutoCommit(false);
            saveReceiptsInSession(reader, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void updateReceipts(Reader reader) {
        try {
            connection.setAutoCommit(false);
            updateReceiptsInSession(reader, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void deleteReceipts(String login) {
        try {
            connection.setAutoCommit(false);
            deleteReceiptsInSession(login, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveReceiptsInSession(Reader reader, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_READER_RECEIPT);
        for (Receipt receipt : reader.getReceipts()) {
            fillPreparedStatement(statement, reader.getLogin(), receipt.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();
        }
    }

    static void updateReceiptsInSession(Reader reader, Connection connection) throws SQLException {
        deleteReceiptsInSession(reader.getLogin(), connection);
        saveReceiptsInSession(reader, connection);
    }

    static void deleteReceiptsInSession(String login, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_RECEIPTS_BY_READER_LOGIN);
        fillPreparedStatement(statement, login);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public List<Subscription> findSubscriptions(String login) {
        List<Subscription> subscriptions = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_READER_SUBSCRIPTIONS);
            fillPreparedStatement(statement, login);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            SubscriptionSqlDao subscriptionSqlDao = new SubscriptionSqlDao(connection);
            while (resultSet.next()) {
                subscriptions.add(subscriptionSqlDao.mapToSubscription(resultSet));
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return subscriptions;
    }

    @Override
    public void saveSubscriptions(Reader reader) {
        try {
            connection.setAutoCommit(false);
            saveSubscriptionsInSession(reader, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void updateSubscriptions(Reader reader) {
        try {
            connection.setAutoCommit(false);
            updateSubscriptionsInSession(reader, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void deleteSubscriptions(String login) {
        try {
            connection.setAutoCommit(false);
            deleteSubscriptionsInSession(login, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveSubscriptionsInSession(Reader reader, Connection connection) {

    }

    static void updateSubscriptionsInSession(Reader reader, Connection connection) {

    }

    static void deleteSubscriptionsInSession(String login, Connection connection) {

    }
}
