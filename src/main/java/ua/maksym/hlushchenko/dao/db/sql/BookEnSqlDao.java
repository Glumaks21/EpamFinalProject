package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.exception.DaoException;

import java.sql.*;
import java.sql.Date;
import java.util.*;

class BookEnSqlDao extends BookSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT id, title, description, author_id, publisher_isbn, date " +
            "FROM book";
    private static final String SQL_SELECT_BY_ID = "SELECT id, title, description, author_id, publisher_isbn, date " +
            "FROM book  " +
            "WHERE id = ?";
    private static final String SQL_SELECT_GENRES_BY_BOOK_ID = "SELECT id, name " +
            "FROM genre g " +
            "JOIN book_has_genre bg ON g.id = bg.genre_id " +
            "WHERE bg.book_id = ?";
    private static final String SQL_INSERT = "INSERT INTO book" +
            "(title, description, author_id, publisher_isbn, date)" +
            "VALUES(?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_BOOK_GENRE = "INSERT INTO book_has_genre(book_id, genre_id) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE book " +
            "SET title = ?, description = ?, author_id = ?, publisher_isbn = ?, date = ?" +
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
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Book> find(Integer id) {
        List<Book> books = mappedQuery(SQL_SELECT_BY_ID, id);
        if (books.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(books.get(0));
    }

    @Override
    public void save(Book book) {
        if (!(book.getAuthor() instanceof LoadProxy)) {
            AuthorSqlDao authorSqlDao = new AuthorEnSqlDao(connection);
            authorSqlDao.save(book.getAuthor());
            Author savedAuthor = authorSqlDao.find(book.getAuthor().getId()).get();
            book.setAuthor(savedAuthor);
        }

        if (!(book.getPublisher() instanceof LoadProxy)) {
            PublisherSqlDao publisherSqlDao = new PublisherSqlDao(connection);
            publisherSqlDao.save(book.getPublisher());
            Publisher savedPublisher = publisherSqlDao.find(book.getPublisher().getIsbn()).get();
            book.setPublisher(savedPublisher);
        }

        try (ResultSet resultSet =  updateQuery(SQL_INSERT,
                    book.getTitle(),
                    book.getDescription(),
                    book.getAuthor().getId(),
                    book.getPublisher().getIsbn(),
                    Date.valueOf(book.getDate()))) {
            if (resultSet.next()) {
                book.setId(resultSet.getInt(1));
            }

            saveGenres(book);
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Book book) {
        updateQuery(SQL_UPDATE_BY_ID,
                book.getTitle(),
                book.getDescription(),
                book.getAuthor().getId(),
                book.getPublisher().getIsbn(),
                Date.valueOf(book.getDate()),
                book.getId());
        updateGenres(book);
    }

    @Override
    public void delete(Integer id) {
        deleteGenres(id);
        updateQuery(SQL_DELETE_BY_ID, id);
    }

    @Override
    public List<Genre> findGenres(Integer id) {
        GenreSqlDao genreSqlDao = new GenreEnSqlDao(connection);
        return mappedQuery(genreSqlDao::mapToEntity, SQL_SELECT_GENRES_BY_BOOK_ID, id);
    }

    @Override
    public void saveGenres(Book book) {
        GenreSqlDao genreSqlDao = new GenreEnSqlDao(connection);
        for (Genre genre :  book.getGenres()) {
            if (!(genre instanceof LoadProxy)) {
                genreSqlDao.save(genre);
            }
            updateQuery(SQL_INSERT_BOOK_GENRE,
                    book.getId(),
                    genre.getId());
        }
    }

    @Override
    public void updateGenres(Book book) {
        deleteGenres(book.getId());
        saveGenres(book);
    }

    @Override
    public void deleteGenres(Integer id) {
        updateQuery(SQL_DELETE_ALL_GENRES_BY_BOOK_ID, id);
    }
}
