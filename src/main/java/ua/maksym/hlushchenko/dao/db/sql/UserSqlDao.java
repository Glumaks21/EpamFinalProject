package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.UserDao;
import ua.maksym.hlushchenko.dao.entity.impl.role.UserImpl;
import ua.maksym.hlushchenko.dao.entity.role.*;;
import ua.maksym.hlushchenko.exception.*;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

class UserSqlDao extends AbstractSqlDao<Integer, User> implements UserDao {
    private static final String SQL_SELECT_ALL = "SELECT id, login, password_hash, role_id " +
            "FROM user";
    private static final String SQL_SELECT_BY_ID = "SELECT id, login, password_hash, role_id " +
            "FROM user " +
            "WHERE id = ?";
    private static final String SQL_SELECT_BY_LOGIN_AND_PASSWORD_HASH =
            "SELECT id, login, password_hash, role_id " +
            "FROM user " +
            "WHERE login = ? AND password_hash = ?";
    private static final String SQL_INSERT = "INSERT INTO user(login, password_hash, role_id) " +
            "VALUES(?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE user " +
            "SET password_hash = ?, role_id = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM user " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(UserSqlDao.class);

    public UserSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected User mapToEntity(ResultSet resultSet) {
        try {
            User user = new UserImpl();

            user.setId(resultSet.getInt("id"));
            user.setLogin(resultSet.getString("login"));
            user.setPasswordHash(resultSet.getString("password_hash"));

            RoleSqlDao roleSqlDao = new RoleSqlDao(connection);
            Role role = roleSqlDao.find(resultSet.getInt("role_id")).get();
            user.setRole(role);
            return (User) Proxy.newProxyInstance(
                    UserSqlDao.class.getClassLoader(),
                    new Class[]{User.class, LoadProxy.class},
                    new LoadHandler<>(user));
        } catch (SQLException | NoSuchElementException e) {
            throw new MappingException(e);
        }
    }

    @Override
    public List<User> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<User> find(Integer id) {
        List<User> users = mappedQuery(SQL_SELECT_BY_ID, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    @Override
    public void save(User user) {
        try (ResultSet resultSet = updateQuery(SQL_INSERT,
                user.getLogin(),
                user.getPasswordHash(),
                user.getRole().getId())) {
            if (resultSet.next()) {
                user.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    @Override
    public void update(User user) {
        updateQuery(SQL_UPDATE_BY_ID,
                user.getPasswordHash(),
                user.getRole().getId(),
                user.getId());
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }

    @Override
    public Optional<User> findByLoginAndPasswordHash(String login, String passwordHash) {
        List<User> users = mappedQuery(SQL_SELECT_BY_LOGIN_AND_PASSWORD_HASH, login, passwordHash);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }
}

