package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.*;

import java.util.Locale;

public class SqlDaoFactory implements DaoFactory {
    @Override
    public <D extends Dao<K, T>, K, T extends Entity> D createDao(Class<T> entityClass) {
        return null;
    }

    @Override
    public RoleSqlDao createRoleDao() {
        return new RoleSqlDao(HikariCPDataSource.getInstance());
    }

    @Override
    public UserSqlDao createUserDao() {
        return new UserSqlDao(HikariCPDataSource.getInstance());
    }

    @Override
    public ReaderSqlDao createReaderDao() {
        return new ReaderSqlDao(HikariCPDataSource.getInstance());
    }

    @Override
    public AuthorSqlDao createAuthorDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new AuthorEnSqlDao(HikariCPDataSource.getInstance());
            case "ek":
                return new AuthorUaSqlDao(HikariCPDataSource.getInstance());
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public PublisherSqlDao createPublisherDao() {
        return new PublisherSqlDao(HikariCPDataSource.getInstance());
    }

    @Override
    public Ge createGenreDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new GenreEnSqlDao(HikariCPDataSource.getInstance());
            case "ek":
                return new GenreUaSqlDao(HikariCPDataSource.getInstance());
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public BookSqlDao createBookDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new BookEnSqlDao(HikariCPDataSource.getInstance());
            case "ek":
                return new BookUaSqlDao(HikariCPDataSource.getInstance());
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public SubscriptionSqlDao createSubscriptionDao() {
        return new SubscriptionSqlDao(HikariCPDataSource.getInstance());
    }

    @Override
    public ReceiptSqlDao createReceiptDao() {
        return new ReceiptSqlDao(HikariCPDataSource.getInstance());
    }
}
