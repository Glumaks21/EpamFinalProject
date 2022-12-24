package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.role.Admin;
import ua.maksym.hlushchenko.dao.entity.impl.role.AdminImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class AdminSqlDao extends UserWithRoleSqlDao<Admin> {
    private static final String SQL_SELECT_ALL = "SELECT id, login, password_hash " +
            "FROM admin a " +
            "JOIN user u ON a.user_id = u.id";
    private static final String SQL_SELECT_BY_ID = "SELECT id, login, password_hash " +
            "FROM admin a " +
            "JOIN user u ON a.user_id = u.id " +
            "WHERE user_id = ?";
    private static final String SQL_INSERT = "INSERT INTO admin(user_id) " +
            "VALUES(?)";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM admin " +
            "WHERE user_id = ?";

    public AdminSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Admin mapToEntity(ResultSet resultSet) {
        try {
            Admin admin = new AdminImpl();
            admin.setId(resultSet.getInt("id"));
            admin.setLogin(resultSet.getString("login"));
            admin.setPasswordHash(resultSet.getString("password_hash"));
            return (Admin) Proxy.newProxyInstance(
                    AdminSqlDao.class.getClassLoader(),
                    new Class[]{Admin.class, LoadProxy.class},
                    new LoadHandler<>(admin));
        } catch (SQLException e) {
            throw new MappingException(e);
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
