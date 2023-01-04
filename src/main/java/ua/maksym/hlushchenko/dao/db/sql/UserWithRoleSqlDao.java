package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.role.User;
import ua.maksym.hlushchenko.exception.DaoException;

import java.sql.*;
import java.util.List;

public abstract class UserWithRoleSqlDao<T extends User> extends AbstractSqlDao<Integer, T> {
    private static final String SQL_INSERT = QueryUtil.createInsert(
            UserSqlDao.SQL_TABLE_NAME,
            List.of(UserSqlDao.SQL_COLUMN_NAME_LOGIN, UserSqlDao.SQL_COLUMN_NAME_PASSWORD_HASH));

    private static final String SQL_UPDATE_BY_ID = QueryUtil.createUpdate(
            UserSqlDao.SQL_TABLE_NAME,
            List.of(UserSqlDao.SQL_COLUMN_NAME_LOGIN, UserSqlDao.SQL_COLUMN_NAME_PASSWORD_HASH),
            List.of(UserSqlDao.SQL_COLUMN_NAME_ID));

    private static final String SQL_DELETE_BY_ID = QueryUtil.createDelete(
            UserSqlDao.SQL_TABLE_NAME,
            List.of(UserSqlDao.SQL_COLUMN_NAME_ID));


    public UserWithRoleSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    public void save(T user) {
        try (ResultSet resultSet = updateQueryWithKeys(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS,
                user.getLogin(),
                user.getPasswordHash())) {
            if (resultSet.next()) {
                user.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(T user) {
        updateQuery(SQL_UPDATE_BY_ID,
                user.getLogin(),
                user.getPasswordHash(),
                user.getId());
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }
}
