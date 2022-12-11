package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.db.entity.impl.role.UserImpl;
import ua.maksym.hlushchenko.db.entity.role.User;

import java.sql.*;
import java.util.*;

public class UserSqlDao extends AbstractSqlDao<String, User> {
    private static final String SQL_SELECT_ALL = "SELECT * FROM user";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM user " +
            "WHERE login = ?";
    private static final String SQL_INSERT = "INSERT INTO user(login, password) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_LOGIN = "UPDATE user SET " +
            "password = ? " +
            "WHERE login = ?";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM user " +
            "WHERE login = ?";

    private static final Logger log = LoggerFactory.getLogger(UserSqlDao.class);

    public UserSqlDao(Connection connection) {
        super(connection);
    }

    User mapToUser(ResultSet resultSet) throws SQLException {
        User user = new UserImpl();
        user.setLogin(resultSet.getString("login"));
        user.setPassword(resultSet.getString("password"));
        return user;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                User user = mapToUser(resultSet);
                users.add(user);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return users;
    }

    @Override
    public Optional<User> find(String id) {
        User user = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = mapToUser(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(user);
    }

    @Override
    public void save(User user) {
        try  {
            connection.setAutoCommit(false);
            saveInSession(user, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(User user) {
        try  {
            connection.setAutoCommit(false);
            updateInSession(user, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void delete(String id) {
        try  {
            connection.setAutoCommit(false);
            deleteInSession(id, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveInSession(User user, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                user.getLogin(),
                user.getPassword());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void updateInSession(User user, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_LOGIN);
        fillPreparedStatement(statement,
                user.getPassword(),
                user.getLogin());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInSession(String id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}

