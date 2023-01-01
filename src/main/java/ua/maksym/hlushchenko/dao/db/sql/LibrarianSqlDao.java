package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.role.Librarian;
import ua.maksym.hlushchenko.dao.entity.impl.role.LibrarianImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class LibrarianSqlDao extends UserWithRoleSqlDao<Librarian> {
    private static final String SQL_SELECT_ALL = "SELECT id, login, password_hash " +
            "FROM librarian l " +
            "JOIN user u ON l.user_id = u.id";
    private static final String SQL_SELECT_BY_ID = "SELECT id, login, password_hash " +
            "FROM librarian l " +
            "JOIN user u ON l.user_id = u.id " +
            "WHERE user_id = ?";
    private static final String SQL_INSERT = "INSERT INTO librarian(user_id) " +
            "VALUES(?)";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM librarian " +
            "WHERE user_id = ?";

    public LibrarianSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Librarian mapToEntity(ResultSet resultSet) {
        try {
            Librarian librarian = new LibrarianImpl();
            librarian.setId(resultSet.getInt("id"));
            librarian.setLogin(resultSet.getString("login"));
            librarian.setPasswordHash(resultSet.getString("password_hash"));
            return (Librarian) Proxy.newProxyInstance(
                    AdminSqlDao.class.getClassLoader(),
                    new Class[]{Librarian.class, LoadProxy.class},
                    new LoadHandler<>(librarian));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }

    @Override
    public List<Librarian> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Librarian> find(Integer id) {
        List<Librarian> librarians = mappedQuery(SQL_SELECT_BY_ID, id);
        if (librarians.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(librarians.get(0));
    }

    @Override
    public void save(Librarian librarian) {
        super.save(librarian);
        updateQuery(SQL_INSERT, librarian.getId());
    }

    @Override
    public void update(Librarian librarian) {
        super.update(librarian);
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
        super.delete(id);
    }
}
