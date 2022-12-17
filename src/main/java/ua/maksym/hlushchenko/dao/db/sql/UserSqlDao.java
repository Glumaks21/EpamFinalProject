package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.entity.impl.role.UserImpl;
import ua.maksym.hlushchenko.dao.entity.role.Role;
import ua.maksym.hlushchenko.dao.entity.role.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class UserSqlDao extends AbstractSqlDao<Integer, User> {
    private static final String SQL_SELECT_ALL = "SELECT id, login, password_hash, role_id FROM user";
    private static final String SQL_SELECT_BY_ID = "SELECT id, login, password_hash, role_id FROM user " +
            "WHERE id = ?";
    private static final String SQL_INSERT = "INSERT INTO user(login, password_hash, role_id) " +
            "VALUES(?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE user SET " +
            "password_hash = ?, role_id = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM user " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(UserSqlDao.class);

    public UserSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected User mapToEntity(ResultSet resultSet) throws SQLException {
        User user = new UserImpl();
        user.setId(resultSet.getInt("id"));
        user.setLogin(resultSet.getString("login"));
        user.setPasswordHash(resultSet.getString("password_hash"));

        DaoFactory daoFactory = new SqlDaoFactory();
        Dao<Integer, Role> userDao = daoFactory.createRoleDao();
        Role role = userDao.find(resultSet.getInt("role_id")).get();
        user.setRole(role);
        return user;
    }

    @Override
    public List<User> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<User> find(Integer id) {
        List<User> users = mappedQueryResult(SQL_SELECT_BY_ID, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    @Override
    public void save(User user) {
        updateInTransaction(UserSqlDao::saveInTransaction, user);
    }

    @Override
    public void update(User user) {
        updateInTransaction(UserSqlDao::updateInTransaction, user);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(UserSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(User user, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                user.getLogin(),
                user.getPasswordHash(),
                user.getRole().getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void updateInTransaction(User user, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                user.getPasswordHash(),
                user.getRole().getId(),
                user.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}

