package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.db.dao.ReceiptDao;
import ua.maksym.hlushchenko.db.entity.*;
import ua.maksym.hlushchenko.db.entity.impl.ReceiptImpl;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class ReceiptSqlDao extends AbstractSqlDao<Integer, Receipt> implements ReceiptDao {
    private static final String SQL_SELECT_ALL = "SELECT * FROM receipt";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM receipt " +
            "WHERE id = ?";
    private static final String SQL_SELECT_RECEIPT_BOOKS = "SELECT * FROM receipt_has_book " +
            "WHERE receipt_id = ?";
    private static final String SQL_INSERT = "INSERT INTO receipt(reader_login, time) " +
            "VALUES(?, ?)";
    private static final String SQL_INSERT_RECEIPT_BOOK = "INSERT INTO receipt_has_book(receipt_id, book_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE receipt SET " +
            "reader_login = ?, time = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM receipt " +
            "WHERE id = ?";
    private static final String SQL_DELETE_RECEIPTS_BOOKS = "DELETE FROM receipt_has_book " +
            "WHERE receipt_id = ?";

    private static final Logger log = LoggerFactory.getLogger(ReceiptSqlDao.class);

    Receipt mapToReceipt(ResultSet resultSet) throws SQLException {
        Receipt receipt = new ReceiptImpl();
        receipt.setId(resultSet.getInt("id"));

        ReaderSqlDao readerDao = new ReaderSqlDao(connection);
        receipt.setReader(readerDao.find(resultSet.getString("reader_login")).get());

        receipt.setDateTime(resultSet.getTimestamp("time").toLocalDateTime());
        return (Receipt) Proxy.newProxyInstance(ReceiptSqlDao.class.getClassLoader(),
                new Class[]{Receipt.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getBooks") &&
                            receipt.getBooks() == null) {
                        receipt.setBooks(findBooks(receipt.getId()));
                    }

                    return method.invoke(receipt, args);
                });
    }

    public ReceiptSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    public List<Receipt> findAll() {
        List<Receipt> receipts = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                receipts.add(mapToReceipt(resultSet));
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return receipts;
    }

    @Override
    public Optional<Receipt> find(Integer id) {
        Receipt receipt = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                receipt = mapToReceipt(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(receipt);
    }

    @Override
    public void save(Receipt receipt) {
        try {
            connection.setAutoCommit(false);
            saveInSession(receipt, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Receipt receipt) {
        try {
            connection.setAutoCommit(false);
            updateInSession(receipt, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            connection.setAutoCommit(false);
            deleteInSession(id, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveInSession(Receipt receipt, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement,
                receipt.getReader().getLogin(),
                Timestamp.valueOf(receipt.getDateTime()));
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        while (resultSet.next()) {
            receipt.setId(resultSet.getInt(1));
        }

        saveBooksInSession(receipt, connection);
    }

    static void updateInSession(Receipt receipt, Connection connection) throws SQLException {
        updateBooksInSession(receipt, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                receipt.getReader().getLogin(),
                Timestamp.valueOf(receipt.getDateTime()),
                receipt.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInSession(Integer id, Connection connection) throws SQLException {
        deleteBooksInSession(id, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public List<Book> findBooks(Integer id) {
        List<Book> books = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_RECEIPT_BOOKS);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            BookSqlDao bookSqlDao = new BookSqlDao(connection);
            while (resultSet.next()) {
                Book book = bookSqlDao.find(resultSet.getInt("book_id")).get();
                books.add(book);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return books;
    }

    @Override
    public void saveBooks(Receipt receipt) {
        try {
            connection.setAutoCommit(false);
            saveBooksInSession(receipt, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void updateBooks(Receipt receipt) {
        try {
            connection.setAutoCommit(false);
            updateBooksInSession(receipt, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void deleteBooks(Integer id) {
        try {
            connection.setAutoCommit(false);
            deleteBooksInSession(id, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveBooksInSession(Receipt receipt, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_RECEIPT_BOOK);
        for (Book book : receipt.getBooks()) {
            fillPreparedStatement(statement,
                    receipt.getId(),
                    book.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();
        }
    }

    static void updateBooksInSession(Receipt receipt, Connection connection) throws SQLException {
        deleteBooksInSession(receipt.getId(), connection);
        saveBooksInSession(receipt, connection);
    }

    static void deleteBooksInSession(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_RECEIPTS_BOOKS);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
