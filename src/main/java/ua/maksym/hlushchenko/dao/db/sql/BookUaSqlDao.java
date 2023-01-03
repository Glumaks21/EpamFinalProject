package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.sql.BookImpl;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

class BookUaSqlDao extends TranslatedBookSqlDao {
    static final String SQL_TABLE_NAME = "book_ua";
    static final String SQL_COLUMN_NAME_ID = "book_id";
    static final String SQL_COLUMN_NAME_TITLE = "title";
    static final String SQL_COLUMN_NAME_DESCRIPTION = "description";

    private static final String SQL_SELECT_ALL = String.format(
            "SELECT %s, b_u.%s as %<s, %s, %s, %s, b_u.%s as %<s " +
            "FROM %s b_u " +
            "JOIN %s b ON b_u.%s = b.%s",
            SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_TITLE,
            BookEnSqlDao.SQL_COLUMN_NAME_AUTHOR, BookEnSqlDao.SQL_COLUMN_NAME_PUBLISHER,
            BookEnSqlDao.SQL_COLUMN_NAME_DATE, SQL_COLUMN_NAME_DESCRIPTION,
            SQL_TABLE_NAME, BookEnSqlDao.SQL_TABLE_NAME,
            SQL_COLUMN_NAME_ID, BookEnSqlDao.SQL_COLUMN_NAME_ID
    );

    private static final String SQL_SELECT_BY_ID = String.format(
            "SELECT %s, b_u.%s as %<s, %s, %s, %s, b_u.%s as %<s " +
            "FROM %s b_u " +
            "JOIN %s b ON b_u.%s = b.%s " +
            "WHERE b.%s = ?",
            SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_TITLE,
            BookEnSqlDao.SQL_COLUMN_NAME_AUTHOR, BookEnSqlDao.SQL_COLUMN_NAME_PUBLISHER,
            BookEnSqlDao.SQL_COLUMN_NAME_DATE, SQL_COLUMN_NAME_DESCRIPTION,
            SQL_TABLE_NAME, BookEnSqlDao.SQL_TABLE_NAME,
            SQL_COLUMN_NAME_ID, BookEnSqlDao.SQL_COLUMN_NAME_ID,
            BookEnSqlDao.SQL_COLUMN_NAME_ID
    );

    private static final String SQL_SELECT_GENRES_BY_BOOK_ID = String.format(
            "SELECT g_u.%s as %<s, %s " +
            "FROM %s g_u " +
            "JOIN %s bg ON g_u.%s = bg.%s " +
            "WHERE bg.%s = ?",
            GenreUaSqlDao.SQL_COLUMN_NAME_ID, GenreUaSqlDao.SQL_COLUMN_NAME_NAME,
            GenreUaSqlDao.SQL_TABLE_NAME, BookEnSqlDao.SQL_GENRES_TABLE_NAME,
            GenreUaSqlDao.SQL_COLUMN_NAME_ID, BookEnSqlDao.SQL_GENRES_COLUMN_NAME_GENRE_ID,
            BookEnSqlDao.SQL_GENRES_COLUMN_NAME_BOOK_ID
    );

    private static final String SQL_INSERT = QueryUtil.createInsert(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_TITLE, SQL_COLUMN_NAME_DESCRIPTION);

    private static final String SQL_UPDATE_BY_ID = QueryUtil.createUpdate(
            SQL_TABLE_NAME, List.of(SQL_COLUMN_NAME_TITLE, SQL_COLUMN_NAME_DESCRIPTION), List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_DELETE_BY_ID = QueryUtil.createDelete(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_ID);

    private static final Logger log = LoggerFactory.getLogger(BookUaSqlDao.class);

    public BookUaSqlDao(Connection connection) {
        super(connection, new Locale("uk", "ua"));
    }

    @Override
    protected Book mapToEntity(ResultSet resultSet) {
        try {
            Book book = new BookImpl();

            book.setId(resultSet.getInt(SQL_COLUMN_NAME_ID));
            book.setTitle(resultSet.getString(SQL_COLUMN_NAME_TITLE));

            AuthorUaSqlDao authorSqlDao = new AuthorUaSqlDao(connection);
            int authorId = resultSet.getInt(BookEnSqlDao.SQL_COLUMN_NAME_AUTHOR);
            Author author = authorSqlDao.find(authorId).get();
            book.setAuthor(author);

            PublisherSqlDao publisherSqlDao = new PublisherSqlDao(connection);
            int publisherId = resultSet.getInt(BookEnSqlDao.SQL_COLUMN_NAME_PUBLISHER);
            Publisher publisher = publisherSqlDao.find(publisherId).get();
            book.setPublisher(publisher);

            book.setDate(resultSet.getDate(BookEnSqlDao.SQL_COLUMN_NAME_DATE).toLocalDate());
            book.setDescription(resultSet.getString(SQL_COLUMN_NAME_DESCRIPTION));
            return (Book) Proxy.newProxyInstance(
                    BookSqlDao.class.getClassLoader(),
                    new Class[]{Book.class, LoadProxy.class},
                    new LazyInitializationHandler(book));
        } catch (SQLException | ConnectionException | NoSuchElementException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }

    private class LazyInitializationHandler extends LoadHandler<Book> {
        private boolean bookInitialised;

        public LazyInitializationHandler(Book wrapped) {
            super(wrapped);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!bookInitialised && method.getName().equals("getGenres")) {
                bookInitialised = true;
                wrapped.setGenres(findGenres(wrapped.getId()));
            }
            return super.invoke(proxy, method, args);
        }
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
