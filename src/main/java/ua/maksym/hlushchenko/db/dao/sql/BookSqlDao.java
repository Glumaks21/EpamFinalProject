package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.db.dao.*;
import ua.maksym.hlushchenko.db.entity.*;
import ua.maksym.hlushchenko.db.entity.impl.*;

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

    public BookSqlDao(Connection connection) {
        super(connection);
    }

    Book mapToBook(ResultSet resultSet) throws SQLException {
        Book book = new BookImpl();

        book.setId(resultSet.getInt("id"));
        book.setTitle(resultSet.getString("title"));

        AuthorSqlDao authorSqlDao = new AuthorSqlDao(connection);
        Author author = authorSqlDao.find(resultSet.getInt("author_id")).get();
        book.setAuthor(author);

        PublisherDao publisherDao = new PublisherSqlDao(connection);
        Publisher publisher = publisherDao.find(resultSet.getString("publisher_isbn")).get();
        book.setPublisher(publisher);

        book.setDate(resultSet.getDate("date").toLocalDate());
        return (Book) Proxy.newProxyInstance(
                BookSqlDao.class.getClassLoader(),
                new Class[]{Book.class},
                (proxy, method, methodArgs) -> {
                    if (method.getName().equals("getGenres") &&
                            book.getGenres() == null) {
                        book.setGenres(findGenres(book.getId()));
                    }

                    return method.invoke(book, methodArgs);
                });
    }

    @Override
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();

            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Book book = mapToBook(resultSet);
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

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);

            log.info("Try to execute:\n" + formatSql(statement));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                book = mapToBook(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(book);
    }

    @Override
    public void save(Book book) {
        try {
            connection.setAutoCommit(false);
            saveBookInSession(book);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Book book) {
        try {
            connection.setAutoCommit(false);
            updateInSession(book);
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
            deleteInSession(id);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public List<Genre> findGenres(Integer id) {
        List<Genre> genres = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_GENRES_BY_BOOK_ID);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                GenreImpl genre = GenreSqlDao.mapToGenre(resultSet);
                genres.add(genre);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return genres;
    }

    @Override
    public void saveGenres(Book book) {
        try {
            connection.setAutoCommit(false);
            saveGenresInSession(book);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void updateGenres(Book book) {
        try {
            connection.setAutoCommit(false);
            updateGenresInSession(book);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void deleteGenres(Integer id) {
        try {
            connection.setAutoCommit(false);
            deleteGenresInSession(id);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    void saveBookInSession(Book book) throws SQLException {
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

        saveGenresInSession(book);
    }

    void updateInSession(Book book) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                book.getTitle(),
                book.getAuthor().getId(),
                book.getPublisher().getIsbn(),
                Date.valueOf(book.getDate()),
                book.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        updateGenresInSession(book);
    }

    void deleteInSession(Integer id) throws SQLException {
        deleteGenresInSession(id);

        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    void saveGenresInSession(Book book) throws SQLException {
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

    void updateGenresInSession(Book book) throws SQLException {
        deleteGenresInSession(book.getId());
        saveGenresInSession(book);
    }

    void deleteGenresInSession(Integer id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_ALL_GENRES_BY_BOOK_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
