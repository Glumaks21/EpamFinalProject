package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.*;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BookSqlDao extends AbstractSqlDao<Integer, Book> implements BookDao {
    private static final String SQL_SELECT_ALL = "SELECT * FROM book";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM book  " +
            "WHERE id = ?";
    private static final String SQL_SELECT_GENRES_BY_BOOK_ID = "SELECT * FROM genre g " +
            "JOIN book_has_genre bg ON g.id = bg.genre_id " +
            "WHERE bg.book_id = ?";
    private static final String SQL_INSERT = "INSERT INTO book(title, author_id, publisher_isbn, date)" +
            "VALUES(?, ?, ?, ?)";
    private static final String SQL_INSERT_BOOK_GENRE = "INSERT INTO book_has_genre(book_id, genre_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE book SET " +
            "title = ?, author_id = ?, publisher_isbn = ?, date = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM book " +
            "WHERE id = ?";
    private static final String SQL_DELETE_ALL_GENRES_BY_BOOK_ID = "DELETE FROM book_has_genre " +
            "WHERE book_id = ?";

    private static final Logger log = LoggerFactory.getLogger(BookSqlDao.class);

    public BookSqlDao(DataSource connection) {
        super(connection);
    }

    @Override
    protected Book mapToEntity(ResultSet resultSet) throws SQLException {
        Book book = new BookImpl();

        book.setId(resultSet.getInt("id"));
        book.setTitle(resultSet.getString("title"));

        AuthorSqlDao authorSqlDao = new AuthorSqlDao(dataSource);
        Author author = authorSqlDao.find(resultSet.getInt("author_id")).get();
        book.setAuthor(author);

        PublisherDao publisherDao = new PublisherSqlDao(dataSource);
        Publisher publisher = publisherDao.find(resultSet.getString("publisher_isbn")).get();
        book.setPublisher(publisher);

        book.setDate(resultSet.getDate("date").toLocalDate());
        return (Book) Proxy.newProxyInstance(
                BookSqlDao.class.getClassLoader(),
                new Class[]{Book.class},
                new LazyInitializationHandler(book));
    }

    private class LazyInitializationHandler implements InvocationHandler {
        private final Book wrapped;
        private boolean bookInitialised;

        public LazyInitializationHandler(Book wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!bookInitialised && method.getName().equals("getGenres")) {
                bookInitialised = true;
                wrapped.setGenres(findGenres(wrapped.getId()));
            }
            return method.invoke(wrapped, args);
        }
    }

    @Override
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Book book = mapToEntity(resultSet);
                books.add(book);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return books;
    }

    @Override
    public Optional<Book> find(Integer id) {
        Book book = null;

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);

            log.info("Try to execute:\n" + formatSql(statement));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                book = mapToEntity(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(book);
    }

    @Override
    public void save(Book book) {
        dmlOperation(BookSqlDao::saveInTransaction, book);
    }

    @Override
    public void update(Book book) {
        dmlOperation(BookSqlDao::updateInTransaction, book);
    }

    @Override
    public void delete(Integer id) {
        dmlOperation(BookSqlDao::deleteInTransaction, id);
    }

    @Override
    public List<Genre> findGenres(Integer id) {
        List<Genre> genres = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_GENRES_BY_BOOK_ID);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            GenreSqlDao genreSqlDao = new GenreSqlDao(dataSource);
            while (resultSet.next()) {
                Genre genre = genreSqlDao.mapToEntity(resultSet);
                genres.add(genre);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return genres;
    }

    @Override
    public void saveGenres(Book book) {
        dmlOperation(BookSqlDao::saveGenresInTransaction, book);
    }

    @Override
    public void updateGenres(Book book) {
        dmlOperation(BookSqlDao::updateGenresInTransaction, book);
    }

    @Override
    public void deleteGenres(Integer id) {
        dmlOperation(BookSqlDao::deleteGenresInTransaction, id);
    }

    static void saveInTransaction(Book book, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement,
                book.getTitle(),
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
