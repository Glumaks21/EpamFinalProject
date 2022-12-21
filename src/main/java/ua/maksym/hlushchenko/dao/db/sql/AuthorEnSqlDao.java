package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Author;

import java.sql.*;
import java.util.*;

public class AuthorEnSqlDao extends AuthorSqlDao  {
    final static String SQL_SELECT_ALL = "SELECT id, name, surname " +
            "FROM author";
    final static String SQL_SELECT_BY_ID = "SELECT id, name, surname " +
            "FROM author " +
            "WHERE id = ?";
    final static String SQL_INSERT = "INSERT INTO author(name, surname) " +
            "VALUES(?, ?)";
    final static String SQL_UPDATE_BY_ID = "UPDATE author " +
            "SET name = ?, surname = ? " +
            "WHERE id = ?";
    final static String SQL_DELETE_BY_ID = "DELETE FROM author " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(AuthorEnSqlDao.class);

    public AuthorEnSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    public List<Author> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Author> find(Integer id) {
        List<Author> authors = mappedQuery(SQL_SELECT_BY_ID, id);
        if (authors.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(authors.get(0));
    }

    @Override
    public void save(Author author) {
        try (ResultSet resultSet = updateQuery(SQL_INSERT,
                author.getName(),
                author.getSurname())) {
            if (resultSet.next()) {
                author.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Author author) {
        updateQuery(SQL_UPDATE_BY_ID,
                author.getName(),
                author.getSurname(),
                author.getId());
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }
}
