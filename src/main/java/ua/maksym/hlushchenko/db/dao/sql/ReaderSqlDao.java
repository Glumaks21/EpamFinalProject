package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.db.dao.ReaderDao;
import ua.maksym.hlushchenko.db.entity.Subscription;
import ua.maksym.hlushchenko.db.entity.roles.Reader;

import java.sql.*;
import java.util.*;

public class ReaderSqlDao extends AbstractSqlDao<String, Reader> implements ReaderDao {
    static String SQL_SELECT_ALL = "SELECT * FROM reader r " +
            "JOIN user u ON r.user_login = u.login";
    static String SQL_SELECT_BY_LOGIN = "SELECT * FROM reader r " +
            "JOIN user u ON r.user_login = u.login WHERE login = ?";
    static String SQL_INSERT = "INSERT INTO reader(user_login, blocked) VALUES(?, ?)";
    static String SQL_UPDATE_BY_LOGIN = "UPDATE reader SET blocked = ? WHERE user_login = ?";
    static String SQL_DELETE_BY_LOGIN = "DELETE FROM reader WHERE user_login = ?";

    private static final Logger log = LoggerFactory.getLogger(ReaderSqlDao.class);

    public ReaderSqlDao(Connection connection) {
        super(connection);
    }

    static Reader mapToReader(ResultSet resultSet) throws SQLException {
        Reader reader = new Reader();
        reader.setBlocked(resultSet.getBoolean("blocked"));
        reader.setUser(UserSqlDao.mapToUser(resultSet));
        return reader;
    }

    @Override
    public List<Reader> findAll() {
        List<Reader> readers = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                Reader reader = mapToReader(resultSet);
                readers.add(reader);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return readers;
    }

    @Override
    public Optional<Reader> find(String id) {
        Reader reader = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);

            log.info("Try to execute:\n" + formatSql(statement));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                reader = mapToReader(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(reader);
    }

    @Override
    public void save(Reader reader) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(UserSqlDao.SQL_INSERT);
            fillPreparedStatement(statement,
                    reader.getUser().getLogin(),
                    reader.getUser().getPassword());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement,
                    reader.getUser().getLogin(),
                    reader.isBlocked());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Reader reader) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_LOGIN);
            fillPreparedStatement(statement,
                    reader.isBlocked(),
                    reader.getUser().getLogin());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void delete(String id) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            statement = connection.prepareStatement(UserSqlDao.SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public Set<Subscription> getSubscriptions() {
        Set<Subscription> subscriptions = new HashSet<>();

        return subscriptions;
    }
}
