package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class AuthorUaSqlDao extends AuthorSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT author_id, name, surname " +
            "FROM author_ua";
    private static final String SQL_SELECT_BY_ID = "SELECT author_id, name, surname " +
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

    public AuthorUaSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Author mapToEntity(ResultSet resultSet) throws SQLException {
        Author author = new AuthorImpl();
        author.setId(resultSet.getInt("id"));
        author.setName(resultSet.getString("name"));
        author.setSurname(resultSet.getString("surname"));
        return author;
    }

    @Override
    public List<Author> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Author> find(Integer id) {
        List<Author> authors = mappedQueryResult(SQL_SELECT_BY_ID, id);
        if (authors.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(authors.get(0));
    }

    @Override
    public void save(Author author) {
        updateInTransaction(AuthorUaSqlDao::saveInTransaction, author);
    }

    @Override
    public void update(Author author) {
        updateInTransaction(AuthorUaSqlDao::updateInTransaction, author);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(AuthorUaSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Author author, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                author.getId(),
                author.getName(),
                author.getSurname());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void updateInTransaction(Author author, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                author.getName(),
                author.getSurname(),
                author.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
