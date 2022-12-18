package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;
import ua.maksym.hlushchenko.exception.MappingException;

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
            return author;
        } catch (SQLException e) {
            throw new MappingException(e);
        }
    }
}
