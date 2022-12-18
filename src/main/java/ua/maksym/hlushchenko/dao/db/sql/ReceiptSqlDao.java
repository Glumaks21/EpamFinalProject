package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.ReceiptDao;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.ReceiptImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.MappingException;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class ReceiptSqlDao extends AbstractSqlDao<Integer, Receipt> implements ReceiptDao<Integer> {
    private static final String SQL_SELECT_ALL = "SELECT id, reader_id, time " +
            "FROM receipt";
    private static final String SQL_SELECT_BY_ID = "SELECT id, reader_id, time " +
            "FROM receipt " +
            "WHERE id = ?";
    private static final String SQL_SELECT_RECEIPT_BOOKS =
            "SELECT id, title, author_id, publisher_isbn, date, description, cover_id " +
            "FROM book b " +
            "JOIN receipt_has_book rhb ON rhb.book_id = b.id" +
            "WHERE rhb.receipt_id = ?";
    private static final String SQL_INSERT = "INSERT INTO receipt(reader_id, time) " +
            "VALUES(?, ?)";
    private static final String SQL_INSERT_RECEIPT_BOOK = "INSERT INTO receipt_has_book(receipt_id, book_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE receipt SET " +
            "reader_id = ?, time = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM receipt " +
            "WHERE id = ?";
    private static final String SQL_DELETE_RECEIPTS_BOOKS = "DELETE FROM receipt_has_book " +
            "WHERE receipt_id = ?";

    private static final Logger log = LoggerFactory.getLogger(ReceiptSqlDao.class);

    @Override
    protected Receipt mapToEntity(ResultSet resultSet) {
        try (SqlDaoFactory sqlDaoFactory = new SqlDaoFactory()) {
            Receipt receipt = new ReceiptImpl();
            receipt.setId(resultSet.getInt("id"));

            ReaderSqlDao readerSqlDao = sqlDaoFactory.createReaderDao();
            receipt.setReader(readerSqlDao.find(resultSet.getInt("reader_id")).get());

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
        } catch (SQLException | ConnectionException | NoSuchElementException e) {
            throw new MappingException(e);
        }
    }

    public ReceiptSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    public List<Receipt> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Receipt> find(Integer id) {
        List<Receipt> receipts = mappedQueryResult(SQL_SELECT_BY_ID, id);
        if (receipts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(receipts.get(0));
    }

    @Override
    public void save(Receipt receipt) {
        updateInTransaction(ReceiptSqlDao::saveInTransaction, receipt);
    }

    @Override
    public void update(Receipt receipt) {
        updateInTransaction(ReceiptSqlDao::updateInTransaction, receipt);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(ReceiptSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Receipt receipt, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement,
                receipt.getReader().getLogin(),
                Timestamp.valueOf(receipt.getDateTime()));
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
            receipt.setId(resultSet.getInt(1));
        }

        saveBooksInTransaction(receipt, connection);
    }

    static void updateInTransaction(Receipt receipt, Connection connection) throws SQLException {
        updateBooksInTransaction(receipt, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                receipt.getReader().getLogin(),
                Timestamp.valueOf(receipt.getDateTime()),
                receipt.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(Integer id, Connection connection) throws SQLException {
        deleteBooksInTransaction(id, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public List<Book> findBooks(Integer id) {
        SqlDaoFactory sqlDaoFactory = new SqlDaoFactory();
            BookSqlDao bookSqlDao = sqlDaoFactory.createBookDao(Locale.ENGLISH);
            return mappedQueryResult(bookSqlDao::mapToEntity, SQL_SELECT_RECEIPT_BOOKS, id);

    }

    @Override
    public void saveBooks(Receipt receipt) {
        updateInTransaction(ReceiptSqlDao::saveBooksInTransaction, receipt);
    }

    @Override
    public void updateBooks(Receipt receipt) {
        updateInTransaction(ReceiptSqlDao::updateBooksInTransaction, receipt);
    }

    @Override
    public void deleteBooks(Integer id) {
        updateInTransaction(ReceiptSqlDao::deleteBooksInTransaction, id);
    }

    static void saveBooksInTransaction(Receipt receipt, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_RECEIPT_BOOK);
        for (Book book : receipt.getBooks()) {
            fillPreparedStatement(statement,
                    receipt.getId(),
                    book.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();
        }
    }

    static void updateBooksInTransaction(Receipt receipt, Connection connection) throws SQLException {
        deleteBooksInTransaction(receipt.getId(), connection);
        saveBooksInTransaction(receipt, connection);
    }

    static void deleteBooksInTransaction(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_RECEIPTS_BOOKS);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
