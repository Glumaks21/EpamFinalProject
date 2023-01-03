package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.UserDao;
import ua.maksym.hlushchenko.dao.entity.role.*;;
import ua.maksym.hlushchenko.exception.*;

import java.sql.*;
import java.util.*;

class UserSqlDao extends AbstractSqlDao<Integer, User> implements UserDao {
    static final String SQL_TABLE_NAME = "user";
    static final String SQL_COLUMN_NAME_ID = "id";
    static final String SQL_COLUMN_NAME_LOGIN = "login";
    static final String SQL_COLUMN_NAME_PASSWORD_HASH = "password_hash";

    private static final String SQL_SELECT_ALL = String.format(
            "SELECT %s, %s, %s, %s, " +
                "CASE " +
                    "WHEN r.%s IS NOT null THEN 1 " +
                    "WHEN a.%s IS NOT null THEN 2 " +
                    "WHEN l.%s IS NOT null THEN 3 " +
                "END as role " +
            "FROM %s u " +
            "LEFT OUTER JOIN %s r ON u.%s = r.%s " +
            "LEFT OUTER JOIN %s a ON u.%s = a.%s " +
            "LEFT OUTER JOIN %s l ON u.%s = l.%s",

            SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_LOGIN, SQL_COLUMN_NAME_PASSWORD_HASH,
            ReaderSqlDao.SQL_COLUMN_NAME_BLOCKED,
            ReaderSqlDao.SQL_COLUMN_NAME_ID, AdminSqlDao.SQL_COLUMN_NAME_ID, LibrarianSqlDao.SQL_COLUMN_NAME_ID,
            SQL_TABLE_NAME,
            ReaderSqlDao.SQL_TABLE_NAME, SQL_COLUMN_NAME_ID, ReaderSqlDao.SQL_COLUMN_NAME_ID,
            AdminSqlDao.SQL_TABLE_NAME, SQL_COLUMN_NAME_ID, AdminSqlDao.SQL_COLUMN_NAME_ID,
            LibrarianSqlDao.SQL_TABLE_NAME, SQL_COLUMN_NAME_ID, LibrarianSqlDao.SQL_COLUMN_NAME_ID);


    private static final String SQL_SELECT_BY_ID =
            "SELECT id, login, password_hash, blocked, " +
                "CASE " +
                    "WHEN r.user_id IS NOT null THEN 1 " +
                    "WHEN a.user_id IS NOT null THEN 2 " +
                    "WHEN l.user_id IS NOT null THEN 3 " +
                "END as role " +
                "FROM user u " +
                "LEFT OUTER JOIN reader r ON u.id = r.user_id " +
                "LEFT OUTER JOIN admin a ON u.id = a.user_id " +
                "LEFT OUTER JOIN librarian l ON u.id = l.user_id " +
            "WHERE id = ?";
    private static final String SQL_SELECT_BY_LOGIN =
            "SELECT id, login, password_hash, blocked, " +
                "CASE " +
                    "WHEN r.user_id IS NOT null THEN 1 " +
                    "WHEN a.user_id IS NOT null THEN 2 " +
                    "WHEN l.user_id IS NOT null THEN 3 " +
                "END as role " +
                "FROM user u " +
                "LEFT OUTER JOIN reader r ON u.id = r.user_id " +
                "LEFT OUTER JOIN admin a ON u.id = a.user_id " +
                "LEFT OUTER JOIN librarian l ON u.id = l.user_id " +
            "WHERE login = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM user " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(UserSqlDao.class);

    public UserSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected User mapToEntity(ResultSet resultSet) {
        try {
            switch (resultSet.getInt("role")) {
                case 1:
                    ReaderSqlDao readerSqlDao = new ReaderSqlDao(connection);
                    return readerSqlDao.mapToEntity(resultSet);
                case 2:
                    AdminSqlDao adminSqlDao = new AdminSqlDao(connection);
                    return adminSqlDao.mapToEntity(resultSet);
                case 3:
                    LibrarianSqlDao librarianSqlDao = new LibrarianSqlDao(connection);
                    return librarianSqlDao.mapToEntity(resultSet);
                default:
                    throw new MappingException("User's role is undefined");
            }
        } catch (SQLException e) {
            throw new MappingException("Can't define user role", e);
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
        if (user instanceof Reader) {
            ReaderSqlDao dao = new ReaderSqlDao(connection);
            dao.save((Reader) user);
        } else if (user instanceof Librarian) {
            LibrarianSqlDao dao = new LibrarianSqlDao(connection);
            dao.save((Librarian) user);
        } else if (user instanceof Admin) {
            AdminSqlDao dao = new AdminSqlDao(connection);
            dao.save((Admin) user);
        } else {
            throw new DaoException("Role is not defined");
        }
    }

    @Override
    public void update(User user) {
        if (user instanceof Reader) {
            ReaderSqlDao dao = new ReaderSqlDao(connection);
            dao.update((Reader) user);
        } else if (user instanceof Librarian) {
            LibrarianSqlDao dao = new LibrarianSqlDao(connection);
            dao.update((Librarian) user);
        } else if (user instanceof Admin) {
            AdminSqlDao dao = new AdminSqlDao(connection);
            dao.update((Admin) user);
        } else {
            throw new DaoException("Role is not defined");
        }
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        List<User> users = mappedQuery(SQL_SELECT_BY_LOGIN, login);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }
}

