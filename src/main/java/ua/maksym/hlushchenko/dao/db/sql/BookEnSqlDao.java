package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.DaoException;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BookEnSqlDao extends BookSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT id, title, description, author_id, publisher_isbn, date " +
            "FROM book";
    private static final String SQL_SELECT_BY_ID = "SELECT id, title, description, author_id, publisher_isbn, date " +
            "FROM book  " +
            "WHERE id = ?";
    private static final String SQL_SELECT_GENRES_BY_BOOK_ID = "SELECT id, name FROM genre g " +
            "JOIN book_has_genre bg ON g.id = bg.genre_id " +
            "WHERE bg.book_id = ?";
    private static final String SQL_INSERT = "INSERT INTO book" +
            "(title, description, author_id, publisher_isbn, date)" +
            "VALUES(?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_BOOK_GENRE = "INSERT INTO book_has_genre(book_id, genre_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE book SET " +
            "title = ?, description = ?, author_id = ?, publisher_isbn = ?, date = ?" +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM book " +
            "WHERE id = ?";
    private static final String SQL_DELETE_ALL_GENRES_BY_BOOK_ID = "DELETE FROM book_has_genre " +
            "WHERE book_id = ?";

    private static final Logger log = LoggerFactory.getLogger(BookEnSqlDao.class);

    public BookEnSqlDao(Connection connection) {
        super(connection, Locale.ENGLISH);
    }

    @Override
    public List<Book> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Book> find(Integer id) {
        List<Book> books = mappedQueryResult(SQL_SELECT_BY_ID, id);
        if (books.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(books.get(0));
    }

    @Override
    public void save(Book book) {
        updateInTransaction(BookEnSqlDao::saveInTransaction, book);
    }

    @Override
    public void update(Book book) {
        updateInTransaction(BookEnSqlDao::updateInTransaction, book);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(BookEnSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Book book, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement,
                book.getTitle(),
                book.getDescription(),
                book.getAuthor().getId(),
                book.getPublisher().getIsbn(),
                Date.valueOf(book.getDate()));
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        while (resultSet.next()) {
            book.setId(resultSet.getInt(1));
        }

        saveGenresInTransaction(book, connection);
    }

    static void updateInTransaction(Book book, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                book.getTitle(),
                book.getDescription(),
                book.getAuthor().getId(),
                book.getPublisher().getIsbn(),
                Date.valueOf(book.getDate()),
                book.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        updateGenresInTransaction(book, connection);
    }

    static void deleteInTransaction(Integer id, Connection connection) throws SQLException {
        deleteGenresInTransaction(id, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    @Override
    public List<Genre> findGenres(Integer id) {
        try (SqlDaoFactory sqlDaoFactory = new SqlDaoFactory()) {
            GenreSqlDao genreSqlDao = sqlDaoFactory.createGenreDao(Locale.ENGLISH);
            return mappedQueryResult(genreSqlDao::mapToEntity, SQL_SELECT_GENRES_BY_BOOK_ID, id);
        } catch (ConnectionException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void saveGenres(Book book) {
        updateInTransaction(BookEnSqlDao::saveGenresInTransaction, book);
    }

    @Override
    public void updateGenres(Book book) {
        updateInTransaction(BookEnSqlDao::updateGenresInTransaction, book);
    }

    @Override
    public void deleteGenres(Integer id) {
        updateInTransaction(BookEnSqlDao::deleteGenresInTransaction, id);
    }


    static void saveGenresInTransaction(Book book, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_BOOK_GENRE);
        List<Genre> genres = book.getGenres();
        for (Genre genre : genres) {
            fillPreparedStatement(statement,
                    book.getId(),
                    genre.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();
        }
    }

    static void updateGenresInTransaction(Book book, Connection connection) throws SQLException {
        deleteGenresInTransaction(book.getId(), connection);
        saveGenresInTransaction(book, connection);
    }

    static void deleteGenresInTransaction(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_ALL_GENRES_BY_BOOK_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
