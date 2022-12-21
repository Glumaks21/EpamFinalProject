package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Author;

import java.sql.*;
import java.util.*;

class AuthorUaSqlDao extends AuthorSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT author_id as id, name, surname " +
            "FROM author_ua";
    private static final String SQL_SELECT_BY_ID = "SELECT author_id as id, name, surname " +
            "FROM author_ua " +
            "WHERE author_id = ?";
    private static final String SQL_INSERT = "INSERT INTO author_ua(author_id, name, surname) " +
            "VALUES(?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE author_ua " +
            "SET name = ?, surname = ? " +
            "WHERE author_id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM author_ua " +
            "WHERE author_id = ?";

    private static final Logger log = LoggerFactory.getLogger(AuthorUaSqlDao.class);

    public AuthorUaSqlDao(Connection connection) {
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
        updateQuery(SQL_INSERT,
                author.getId(),
                author.getName(),
                author.getSurname());
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
