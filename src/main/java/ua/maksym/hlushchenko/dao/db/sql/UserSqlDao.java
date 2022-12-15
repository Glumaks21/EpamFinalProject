package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.entity.impl.role.UserImpl;
import ua.maksym.hlushchenko.dao.entity.role.User;

import javax.sql.DataSource;
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

    public UserSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected User mapToEntity(ResultSet resultSet) throws SQLException {
        User user = new UserImpl();
        user.setLogin(resultSet.getString("login"));
        user.setPassword(resultSet.getString("password"));
        return user;
    }

    @Override
    public List<User> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<User> find(String login) {
        List<User> users = mappedQueryResult(SQL_SELECT_BY_LOGIN, login);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    @Override
    public void save(User user) {
        dmlOperation(UserSqlDao::saveInTransaction, user);
    }

    @Override
    public void update(User user) {
        dmlOperation(UserSqlDao::updateInTransaction, user);
    }

    @Override
    public void delete(String id) {
        dmlOperation(UserSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(User user, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                user.getLogin(),
                user.getPassword());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void updateInTransaction(User user, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_LOGIN);
        fillPreparedStatement(statement,
                user.getPassword(),
                user.getLogin());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(String id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}

