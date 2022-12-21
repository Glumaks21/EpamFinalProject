package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.*;

import java.sql.*;
import java.util.*;

public class BookUaSqlDao extends TranslatedBookSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT " +
            "id, b_u.title as title, b_u.description as description, author_id, publisher_isbn, date " +
            "FROM book_ua b_u " +
            "JOIN book b ON b_u.book_id = b.id";
    private static final String SQL_SELECT_BY_ID = "SELECT " +
            "id, b_u.title as title, b_u.description as description, author_id, publisher_isbn, date " +
            "FROM book_ua b_u " +
            "JOIN book b ON b_u.book_id = b.id " +
            "WHERE b.id = ?";
    private static final String SQL_SELECT_GENRES_BY_BOOK_ID = "SELECT g.genre_id as id, name " +
            "FROM genre_ua g " +
            "JOIN book_has_genre bg ON g.genre_id = bg.genre_id " +
            "WHERE bg.book_id = ?";
    private static final String SQL_INSERT = "INSERT INTO book_ua" +
            "(book_id, title, description) " +
            "VALUES(?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE book_ua " +
            "SET title = ?, description = ? " +
            "WHERE book_id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM book_ua " +
            "WHERE book_id = ?";

    private static final Logger log = LoggerFactory.getLogger(BookUaSqlDao.class);

    public BookUaSqlDao(Connection connection) {
        super(connection, new Locale("uk", "ua"));
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
        updateQuery(SQL_INSERT,
                book.getId(),
                book.getTitle(),
                book.getDescription());
        saveGenres(book);
    }

    @Override
    public void update(Book book) {
        super.update(book);
        updateQuery(SQL_UPDATE_BY_ID,
                book.getTitle(),
                book.getDescription(),
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
        GenreUaSqlDao genreUaSqlDao = new GenreUaSqlDao(connection);
        return mappedQuery(genreUaSqlDao::mapToEntity, SQL_SELECT_GENRES_BY_BOOK_ID, id);
    }
}
