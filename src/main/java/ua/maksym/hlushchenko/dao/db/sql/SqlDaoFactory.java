package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.role.Admin;
import ua.maksym.hlushchenko.dao.entity.role.Librarian;
import ua.maksym.hlushchenko.dao.entity.role.User;

import java.sql.*;
import java.util.*;

public class SqlDaoFactory implements DaoFactory {
    private final Connection connection;

    public SqlDaoFactory(Connection connection) {
        this.connection = connection;
    }

    private static final Logger log = LoggerFactory.getLogger(SqlDaoFactory.class);

    @Override
    public UserSqlDao createUserDao() {
        return new UserSqlDao(connection);
    }

    @Override
    public ReaderSqlDao createReaderDao() {
        return new ReaderSqlDao(connection);
    }

    @Override
    public Dao<Integer, Librarian> createLibrarianDao() {
        return new LibrarianSqlDao(connection);
    }

    @Override
    public Dao<Integer, Admin> createAdminDao() {
        return new AdminSqlDao(connection);
    }

    @Override
    public AuthorSqlDao createAuthorDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new AuthorEnSqlDao(connection);
            case "uk":
                return new AuthorUaSqlDao(connection);
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public PublisherSqlDao createPublisherDao() {
        return new PublisherSqlDao(connection);
    }

    @Override
    public GenreSqlDao createGenreDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new GenreEnSqlDao(connection);
            case "uk":
                return new GenreUaSqlDao(connection);
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public BookSqlDao createBookDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new BookEnSqlDao(connection);
            case "uk":
                return new BookUaSqlDao(connection);
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public SubscriptionSqlDao createSubscriptionDao() {
        return new SubscriptionSqlDao(connection);
    }

    @Override
    public ReceiptSqlDao createReceiptDao() {
        return new ReceiptSqlDao(connection);
    }
}
