package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.*;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BookUaSqlDao extends BookSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT book_id as id, title, author_id, publisher_isbn, date, description " +
            "FROM book_ua";
    private static final String SQL_SELECT_BY_ID = "SELECT book_id as id, title, author_id, publisher_isbn, date, description " +
            "FROM book_ua  " +
            "WHERE id = ?";
    private static final String SQL_SELECT_GENRES_BY_BOOK_ID = "SELECT id, name " +
            "FROM genre g " +
            "JOIN book_has_genre bg ON g.id = bg.genre_id " +
            "WHERE bg.book_id = ?";
    private static final String SQL_INSERT = "INSERT INTO book_ua" +
            "(book_id, title, author_ua_id, publisher_isbn, description, date, cover_id)" +
            "VALUES(?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_BOOK_GENRE = "INSERT INTO book_ua_has_genre_ua(book_ua_id, genre_ua_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE book_ua " +
            "SET title = ?, author_ua_id = ?, publisher_isbn = ?, description = ?, date = ?, cover_id = ? " +
            "WHERE book_id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM book_ua " +
            "WHERE book_id = ?";
    private static final String SQL_DELETE_ALL_GENRES_BY_BOOK_ID = "DELETE FROM book_ua_has_genre_ua " +
            "WHERE book_ua_id = ?";

    private static final Logger log = LoggerFactory.getLogger(BookUaSqlDao.class);

    public BookUaSqlDao(DataSource dataSource) {
        super(dataSource);
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

    @Override
    public List<Genre> findGenres(Integer id) {
        SqlDaoFactory sqlDaoFactory = new SqlDaoFactory();
        AbstractSqlDao<Integer, Genre> genreSqlDao = sqlDaoFactory.createGenreDao(Locale.ENGLISH);
        return mappedQueryResult(genreSqlDao::mapToEntity, SQL_SELECT_GENRES_BY_BOOK_ID, id);
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

    static void saveInTransaction(Book book, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                book.getTitle(),
                book.getAuthor().getId(),
                book.getPublisher().getIsbn(),
                Date.valueOf(book.getDate()));
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        saveGenresInTransaction(book, connection);
    }

    static void updateInTransaction(Book book, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                book.getTitle(),
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
