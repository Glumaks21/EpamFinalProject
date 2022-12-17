package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import javax.sql.*;
import java.sql.*;
import java.util.*;

public class AuthorEnSqlDao extends AuthorSqlDao  {
    private final static String SQL_SELECT_ALL = "SELECT id, name, surname " +
            "FROM author";
    private final static String SQL_SELECT_BY_ID = "SELECT id, name, surname " +
            "FROM author " +
            "WHERE id = ?";
    private final static String SQL_INSERT = "INSERT INTO author(name, surname) " +
            "VALUES(?, ?)";
    private final static String SQL_UPDATE_BY_ID = "UPDATE author " +
            "SET name = ?, surname = ? " +
            "WHERE id = ?";
    private final static String SQL_DELETE_BY_ID = "DELETE FROM author " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(AuthorEnSqlDao.class);

    public AuthorEnSqlDao(DataSource dataSource) {
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
        updateInTransaction(AuthorEnSqlDao::saveInTransaction, author);
    }

    @Override
    public void update(Author author) {
        updateInTransaction(AuthorEnSqlDao::updateInTransaction, author);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(AuthorEnSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Author author, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement,
                author.getName(),
                author.getSurname());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
            author.setId(resultSet.getInt(1));
        }
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
