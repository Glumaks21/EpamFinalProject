package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.BookDao;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.BookImpl;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.*;
import java.sql.*;
import java.util.Locale;
import java.util.NoSuchElementException;

public abstract class BookSqlDao extends AbstractSqlDao<Integer, Book> implements BookDao<Integer> {
    private final Locale locale;

    public BookSqlDao(Connection connection, Locale locale) {
        super(connection);
        this.locale = locale;
    }
    @Override
    protected Book mapToEntity(ResultSet resultSet) {
        try (SqlDaoFactory sqlDaoFactory = new SqlDaoFactory()) {
            Book book = new BookImpl();

            book.setId(resultSet.getInt("id"));
            book.setTitle(resultSet.getString("title"));
            book.setDescription(resultSet.getString("description"));

            AuthorSqlDao authorSqlDao = sqlDaoFactory.createAuthorDao(locale);
            Author author = authorSqlDao.find(resultSet.getInt("author_id")).get();
            book.setAuthor(author);

            PublisherSqlDao publisherSqlDao = sqlDaoFactory.createPublisherDao();
            Publisher publisher = publisherSqlDao.find(resultSet.getString("publisher_isbn")).get();
            book.setPublisher(publisher);

            book.setDate(resultSet.getDate("date").toLocalDate());
            return (Book) Proxy.newProxyInstance(
                    BookSqlDao.class.getClassLoader(),
                    new Class[]{Book.class},
                    new LazyInitializationHandler(book));
        } catch (SQLException | ConnectionException | NoSuchElementException e) {
            throw new MappingException(e);
        }
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
}
