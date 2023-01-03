package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.sql.BookImpl;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.sql.Date;
import java.util.*;

class BookEnSqlDao extends BookSqlDao {
    static final String SQL_TABLE_NAME = "book";
    static final String SQL_COLUMN_NAME_ID = "id";
    static final String SQL_COLUMN_NAME_TITLE = "title";
    static final String SQL_COLUMN_NAME_AUTHOR = "author_id";
    static final String SQL_COLUMN_NAME_PUBLISHER = "publisher_id";
    static final String SQL_COLUMN_NAME_DATE = "publication_date";
    static final String SQL_COLUMN_NAME_DESCRIPTION = "description";

    static final String SQL_GENRES_TABLE_NAME = "book_has_genre";
    static final String SQL_GENRES_COLUMN_NAME_BOOK_ID = "book_id";
    static final String SQL_GENRES_COLUMN_NAME_GENRE_ID = "genre_id";

    private static final String SQL_SELECT_ALL = QueryUtil.createSelect(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_TITLE, SQL_COLUMN_NAME_AUTHOR,
            SQL_COLUMN_NAME_PUBLISHER, SQL_COLUMN_NAME_DATE, SQL_COLUMN_NAME_DESCRIPTION);

    private static final String SQL_SELECT_BY_ID = QueryUtil.createSelectWithConditions(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_TITLE, SQL_COLUMN_NAME_AUTHOR,
                    SQL_COLUMN_NAME_PUBLISHER, SQL_COLUMN_NAME_DATE, SQL_COLUMN_NAME_DESCRIPTION),
            List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_SELECT_GENRES_BY_BOOK_ID = String.format(
            "SELECT %s, %s FROM %s g " +
            "JOIN %s bg ON g.%s = bg.%s " +
            "WHERE bg.%s = ?",
            GenreEnSqlDao.SQL_COLUMN_NAME_ID, GenreEnSqlDao.SQL_COLUMN_NAME_NAME, GenreEnSqlDao.SQL_TABLE_NAME,
            SQL_GENRES_TABLE_NAME, GenreEnSqlDao.SQL_COLUMN_NAME_ID, SQL_GENRES_COLUMN_NAME_GENRE_ID,
            SQL_GENRES_COLUMN_NAME_BOOK_ID);

    private static final String SQL_INSERT = QueryUtil.createInsert(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_TITLE, SQL_COLUMN_NAME_AUTHOR,
            SQL_COLUMN_NAME_PUBLISHER, SQL_COLUMN_NAME_DATE, SQL_COLUMN_NAME_DESCRIPTION);

    private static final String SQL_INSERT_BOOK_GENRE = QueryUtil.createInsert(
            SQL_GENRES_TABLE_NAME, SQL_GENRES_COLUMN_NAME_BOOK_ID, SQL_GENRES_COLUMN_NAME_GENRE_ID);

    private static final String SQL_UPDATE_BY_ID = QueryUtil.createUpdate(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_TITLE, SQL_COLUMN_NAME_AUTHOR, SQL_COLUMN_NAME_PUBLISHER,
                    SQL_COLUMN_NAME_DATE, SQL_COLUMN_NAME_DESCRIPTION),
            List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_DELETE_BY_ID = QueryUtil.createDelete(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_ID);

    private static final String SQL_DELETE_ALL_GENRES_BY_BOOK_ID = QueryUtil.createDelete(
            SQL_GENRES_TABLE_NAME, SQL_GENRES_COLUMN_NAME_BOOK_ID);


    private static final Logger log = LoggerFactory.getLogger(BookEnSqlDao.class);

    public BookEnSqlDao(Connection connection) {
        super(connection, Locale.ENGLISH);
    }

    @Override
    protected Book mapToEntity(ResultSet resultSet) {
        try {
            Book book = new BookImpl();

            book.setId(resultSet.getInt(SQL_COLUMN_NAME_ID));
            book.setTitle(resultSet.getString(SQL_COLUMN_NAME_TITLE));

            AuthorEnSqlDao authorSqlDao = new AuthorEnSqlDao(connection);
            Author author = authorSqlDao.find(resultSet.getInt(SQL_COLUMN_NAME_AUTHOR)).get();
            book.setAuthor(author);

            PublisherSqlDao publisherSqlDao = new PublisherSqlDao(connection);
            Publisher publisher = publisherSqlDao.find(resultSet.getInt(SQL_COLUMN_NAME_PUBLISHER)).get();
            book.setPublisher(publisher);

            book.setDate(resultSet.getDate(SQL_COLUMN_NAME_DATE).toLocalDate());
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
        if (!(book.getAuthor() instanceof LoadProxy)) {
            AuthorSqlDao authorSqlDao = new AuthorEnSqlDao(connection);
            authorSqlDao.save(book.getAuthor());
            Author savedAuthor = authorSqlDao.find(book.getAuthor().getId()).get();
            book.setAuthor(savedAuthor);
        }

        if (!(book.getPublisher() instanceof LoadProxy)) {
            PublisherSqlDao publisherSqlDao = new PublisherSqlDao(connection);
            publisherSqlDao.save(book.getPublisher());
            Publisher savedPublisher = publisherSqlDao.find(book.getPublisher().getId()).get();
            book.setPublisher(savedPublisher);
        }

        try (ResultSet resultSet =  updateQueryWithKeys(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS,
                book.getTitle(),
                book.getAuthor().getId(),
                book.getPublisher().getId(),
                Date.valueOf(book.getDate()),
                book.getDescription())) {
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
                book.getAuthor().getId(),
                book.getPublisher().getId(),
                Date.valueOf(book.getDate()),
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
        GenreEnSqlDao dao = new GenreEnSqlDao(connection);
        return mappedQuery(dao::mapToEntity, SQL_SELECT_GENRES_BY_BOOK_ID, id);
    }

    @Override
    public void saveGenres(Book book) {
        GenreEnSqlDao dao = new GenreEnSqlDao(connection);
        for (Genre genre :  book.getGenres()) {
            if (!(genre instanceof LoadProxy)) {
                dao.save(genre);
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
