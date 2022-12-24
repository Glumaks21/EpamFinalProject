package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.ReceiptDao;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.ReceiptImpl;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

class ReceiptSqlDao extends AbstractSqlDao<Integer, Receipt> implements ReceiptDao {
    private static final String SQL_SELECT_ALL = "SELECT id, reader_id, time " +
            "FROM receipt";
    private static final String SQL_SELECT_BY_ID = "SELECT id, reader_id, time " +
            "FROM receipt " +
            "WHERE id = ?";
    private static final String SQL_SELECT_BY_READER_ID = "SELECT id, reader_id, time " +
            "FROM receipt " +
            "WHERE reader_id = ?";
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
    private static final String SQL_DELETE_READER_ID = "DELETE FROM receipt " +
            "WHERE reader_id = ?";
    private static final String SQL_DELETE_RECEIPTS_BOOKS = "DELETE FROM receipt_has_book " +
            "WHERE receipt_id = ?";

    private static final Logger log = LoggerFactory.getLogger(ReceiptSqlDao.class);

    @Override
    protected Receipt mapToEntity(ResultSet resultSet) {
        try {
            Receipt receipt = new ReceiptImpl();
            receipt.setId(resultSet.getInt("id"));

            ReaderSqlDao readerSqlDao = new ReaderSqlDao(connection);
            receipt.setReader(readerSqlDao.find(resultSet.getInt("reader_id")).get());

            receipt.setDateTime(resultSet.getTimestamp("time").toLocalDateTime());
            return (Receipt) Proxy.newProxyInstance(ReceiptSqlDao.class.getClassLoader(),
                    new Class[]{Receipt.class, LoadProxy.class},
                    new LazyInitializationHandler(receipt));
        } catch (SQLException | ConnectionException | NoSuchElementException e) {
            throw new MappingException(e);
        }
    }

    private class LazyInitializationHandler extends LoadHandler<Receipt> {
        private boolean bookInitialised;

        public LazyInitializationHandler(Receipt wrapped) {
            super(wrapped);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!bookInitialised && method.getName().equals("getBooks")) {
                bookInitialised = true;
                wrapped.setBooks(findBooks(wrapped.getId()));
            }
            return super.invoke(proxy, method, args);
        }
    }

    public ReceiptSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    public List<Receipt> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Receipt> find(Integer id) {
        List<Receipt> receipts = mappedQuery(SQL_SELECT_BY_ID, id);
        if (receipts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(receipts.get(0));
    }

    @Override
    public void save(Receipt receipt) {
        try (ResultSet resultSet = updateQuery(SQL_INSERT,
                    receipt.getReader().getId(),
                    Timestamp.valueOf(receipt.getDateTime()))) {
            if (resultSet.next()) {
                receipt.setId(resultSet.getInt(1));
            }

            saveBooks(receipt);
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Receipt receipt) {
        updateBooks(receipt);
        updateQuery(SQL_UPDATE_BY_ID,
                receipt.getReader().getId(),
                Timestamp.valueOf(receipt.getDateTime()),
                receipt.getId());
    }

    @Override
    public void delete(Integer id) {
        deleteBooks(id);
        updateQuery(SQL_DELETE_BY_ID, id);
    }

    @Override
    public Optional<Receipt> findByReaderId(int id) {
        List<Receipt> receipts = mappedQuery(SQL_SELECT_BY_READER_ID, id);
        if (receipts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(receipts.get(0));
    }

    @Override
    public void deleteByReaderId(int id) {
        deleteBooks(id);
        updateQuery(SQL_DELETE_READER_ID, id);
    }

    @Override
    public List<Book> findBooks(Integer id) {
        BookSqlDao bookSqlDao = new BookEnSqlDao(connection);
        return mappedQuery(bookSqlDao::mapToEntity, SQL_SELECT_RECEIPT_BOOKS, id);
    }

    @Override
    public void saveBooks(Receipt receipt) {
        for (Book book : receipt.getBooks()) {
            updateQuery(SQL_INSERT_RECEIPT_BOOK,
                    receipt.getId(),
                    book.getId());
        }
    }

    @Override
    public void updateBooks(Receipt receipt) {
        deleteBooks(receipt.getId());
        saveBooks(receipt);
    }

    @Override
    public void deleteBooks(Integer id) {
        updateQuery(SQL_DELETE_RECEIPTS_BOOKS, id);
    }
}
