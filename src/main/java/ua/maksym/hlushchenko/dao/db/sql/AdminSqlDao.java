package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.role.Admin;
import ua.maksym.hlushchenko.dao.entity.sql.role.AdminImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class AdminSqlDao extends UserWithRoleSqlDao<Admin> {
    static final String SQL_TABLE_NAME = "admin";
    static final String SQL_COLUMN_NAME_ID = "user_id";

    private static final String SQL_SELECT_ALL = String.format(
            "SELECT %s, %s, %s FROM %s a " +
            "JOIN %s u ON a.%s = u.%s",
            SQL_COLUMN_NAME_ID, UserSqlDao.SQL_COLUMN_NAME_LOGIN, UserSqlDao.SQL_COLUMN_NAME_PASSWORD_HASH,
            SQL_TABLE_NAME, UserSqlDao.SQL_TABLE_NAME, SQL_COLUMN_NAME_ID, UserSqlDao.SQL_COLUMN_NAME_ID);

    private static final String SQL_SELECT_BY_ID = String.format(
            "SELECT %s, %s, %s FROM %s a " +
            "JOIN %s u ON a.%s = u.%s " +
            "WHERE %s = ?",
            SQL_COLUMN_NAME_ID, UserSqlDao.SQL_COLUMN_NAME_LOGIN, UserSqlDao.SQL_COLUMN_NAME_PASSWORD_HASH,
            SQL_TABLE_NAME, UserSqlDao.SQL_TABLE_NAME, SQL_COLUMN_NAME_ID, UserSqlDao.SQL_COLUMN_NAME_ID,
            SQL_COLUMN_NAME_ID);

    private static final String SQL_INSERT = QueryUtil.createInsert(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_ID);

    private static final String SQL_DELETE_BY_ID = QueryUtil.createDelete(
            SQL_TABLE_NAME, SQL_COLUMN_NAME_ID);

    public AdminSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Admin mapToEntity(ResultSet resultSet) {
        try {
            Admin admin = new AdminImpl();
            admin.setId(resultSet.getInt(SQL_COLUMN_NAME_ID));
            admin.setLogin(resultSet.getString(UserSqlDao.SQL_COLUMN_NAME_LOGIN));
            admin.setPasswordHash(resultSet.getString(UserSqlDao.SQL_COLUMN_NAME_PASSWORD_HASH));
            return (Admin) Proxy.newProxyInstance(
                    AdminSqlDao.class.getClassLoader(),
                    new Class[]{Admin.class, LoadProxy.class},
                    new LoadHandler<>(admin));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }

    @Override
    public List<Admin> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Admin> find(Integer id) {
        List<Admin> admins = mappedQuery(SQL_SELECT_BY_ID, id);
        if (admins.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(admins.get(0));
    }

    @Override
    public void save(Admin admin) {
        super.save(admin);
        updateQuery(SQL_INSERT, admin.getId());
    }

    @Override
    public void update(Admin admin) {
        super.update(admin);
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
        super.delete(id);
    }
}
