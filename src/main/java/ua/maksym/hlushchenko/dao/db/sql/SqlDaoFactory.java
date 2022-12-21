package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.exception.ConnectionException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class SqlDaoFactory implements DaoFactory, AutoCloseable {
    private static final DataSource dataSource = HikariCPDataSource.getInstance();
    private final List<Connection> reserved = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(SqlDaoFactory.class);

    @Override
    public RoleSqlDao createRoleDao() {
        return new RoleSqlDao(reserveConnection());
    }

    @Override
    public UserSqlDao createUserDao() {
        return new UserSqlDao(reserveConnection());
    }

    @Override
    public ReaderSqlDao createReaderDao() {
        return new ReaderSqlDao(reserveConnection());
    }

    @Override
    public AuthorSqlDao createAuthorDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new AuthorEnSqlDao(reserveConnection());
            case "uk":
                return new AuthorUaSqlDao(reserveConnection());
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public PublisherSqlDao createPublisherDao() {
        return new PublisherSqlDao(reserveConnection());
    }

    @Override
    public GenreSqlDao createGenreDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new GenreEnSqlDao(reserveConnection());
            case "uk":
                return new GenreUaSqlDao(reserveConnection());
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public BookSqlDao createBookDao(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return new BookEnSqlDao(reserveConnection());
            case "uk":
                return new BookUaSqlDao(reserveConnection());
            default:
                throw new IllegalArgumentException("Locale not defined");
        }
    }

    @Override
    public SubscriptionSqlDao createSubscriptionDao() {
        return new SubscriptionSqlDao(reserveConnection());
    }

    @Override
    public ReceiptSqlDao createReceiptDao() {
        return new ReceiptSqlDao(reserveConnection());
    }

    @Override
    public void close() {
        try {
            log.info("Try to close connections");
            for (Connection connection : reserved) {
                connection.close();
            }
            log.info("All connections closed");
        } catch (SQLException e) {
            log.warn("Unsuccessful connection closing: " + e.getMessage());
            throw new ConnectionException(e);
        }
    }

    private Connection reserveConnection() {
        try {
            log.info("Try to reserve connection");
            Connection connection = dataSource.getConnection();
            reserved.add(connection);
            log.info("Connection successfully reserved");
            return connection;
        } catch (SQLException e) {
            log.warn("Unsuccessful connection reservation: " + e.getMessage());
            throw new ConnectionException(e);
        }
    }
}
