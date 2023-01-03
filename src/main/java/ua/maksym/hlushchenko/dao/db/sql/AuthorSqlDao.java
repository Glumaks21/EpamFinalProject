package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.sql.AuthorImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;

public abstract class AuthorSqlDao extends AbstractSqlDao<Integer, Author> {
    public AuthorSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Author mapToEntity(ResultSet resultSet) {
        try {
            Author author = new AuthorImpl();
            author.setId(resultSet.getInt("id"));
            author.setName(resultSet.getString("name"));
            author.setSurname(resultSet.getString("surname"));
            return (Author) Proxy.newProxyInstance(
                    AuthorSqlDao.class.getClassLoader(),
                    new Class[]{Author.class, LoadProxy.class},
                    new LoadHandler<>(author));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }
}
