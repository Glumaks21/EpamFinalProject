package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.BookDao;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.PublisherDao;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Publisher;
import ua.maksym.hlushchenko.dao.entity.impl.BookImpl;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public abstract class BookSqlDao extends AbstractSqlDao<Integer, Book> implements BookDao<Integer> {
    public BookSqlDao(DataSource dataSource) {
        super(dataSource);
    }
    @Override
    protected Book mapToEntity(ResultSet resultSet) throws SQLException {
        Book book = new BookImpl();

        book.setId(resultSet.getInt("id"));
        book.setTitle(resultSet.getString("title"));
        book.setDescription(resultSet.getString("description"));

        DaoFactory daoFactory = new SqlDaoFactory();

        Dao<Integer, Author> authorSqlDao = daoFactory.createAuthorDao(Locale.ENGLISH);
        Author author = authorSqlDao.find(resultSet.getInt("author_id")).get();
        book.setAuthor(author);

        PublisherDao<String> publisherDao = daoFactory.createPublisherDao();
        Publisher publisher = publisherDao.find(resultSet.getString("publisher_isbn")).get();
        book.setPublisher(publisher);

        book.setDate(resultSet.getDate("date").toLocalDate());
        return (Book) Proxy.newProxyInstance(
                BookUaSqlDao.class.getClassLoader(),
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
}
