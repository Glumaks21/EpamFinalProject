package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.Logger;
import org.slf4j.*;
import ua.maksym.hlushchenko.db.dao.ReceiptDao;
import ua.maksym.hlushchenko.db.entity.*;
import ua.maksym.hlushchenko.db.entity.model.ReceiptModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReceiptSqlDao extends AbstractSqlDao<Integer, Receipt> implements ReceiptDao {
    private static final String SQL_SELECT_ALL = "SELECT * FROM receipt rc " +
            "JOIN reader rd ON rc.reader_login = rd.user_login " +
            "JOIN user u ON rd.user_login = u.login";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM receipt rc " +
            "JOIN reader rd ON rc.reader_login = rd.user_login " +
            "JOIN user u ON rd.user_login = u.login " +
            "WHERE rc.id = ?";
    private static final String SQL_SELECT_BOOKS_BY_RECEIPT_ID = "SELECT * FROM receipt_has_book rhp " +
            "JOIN book b ON rhp.book_id = b.id " +
            "JOIN author a ON b.author_id = a.id " +
            "JOIN publisher p on b.publisher_isbn = p.isbn " +
            "WHERE receipt_id = ?";
    private static final String SQL_INSERT = "INSERT INTO receipt(reader_login, time) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE receipt SET " +
            "reader_login = ?, " +
            "time = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM receipt " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(ReceiptSqlDao.class);

    Receipt mapToReceipt(ResultSet resultSet) throws SQLException {
        Receipt receipt = new ReceiptModel();
        receipt.setId(resultSet.getInt("id"));

        ReaderSqlDao readerDao = new ReaderSqlDao(connection);
        receipt.setReader(readerDao.mapToReader(resultSet));

        receipt.setDateTime(resultSet.getTimestamp("time").toLocalDateTime());
        return receipt;
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
                Receipt receipt = mapToReceipt(resultSet);
                receipts.add(receipt);
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

            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
            fillPreparedStatement(statement,
                    receipt.getReader().getLogin(),
                    Timestamp.valueOf(receipt.getDateTime()),
                    receipt.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

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

            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public List<Book> findBooks(Integer id) {
        List<Book> books = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BOOKS_BY_RECEIPT_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            BookSqlDao bookSqlDao = new BookSqlDao(connection);
            while (resultSet.next()) {
                Book book = bookSqlDao.mapToBook(resultSet);
                books.add(book);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return books;
    }

    @Override
    public void saveBooks(Receipt receipt) {

    }

    @Override
    public void updateBooks(Receipt receipt) {

    }

    @Override
    public void deleteBooks(Integer integer) {

    }
}
