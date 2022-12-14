package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class AuthorSqlDao extends AbstractSqlDao<Integer, Author> {
    static String SQL_SELECT_ALL = "SELECT * FROM author";
    static String SQL_SELECT_BY_ID = "SELECT * FROM author " +
            "WHERE id = ?";
    static String SQL_INSERT = "INSERT INTO author(name, surname) " +
            "VALUES(?, ?)";
    static String SQL_UPDATE_BY_ID = "UPDATE author SET " +
            "name = ?, surname = ? " +
            "WHERE id = ?";
    static String SQL_DELETE_BY_ID = "DELETE FROM author " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(AuthorSqlDao.class);

    public AuthorSqlDao(DataSource dataSource) {
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
        List<Author> authors = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Author author = mapToEntity(resultSet);
                authors.add(author);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return authors;
    }

    @Override
    public Optional<Author> find(Integer id) {
        Author author = null;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                author = mapToEntity(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(author);
    }

    @Override
    public void save(Author author) {
        dmlOperation(AuthorSqlDao::saveInTransaction, author);
    }

    @Override
    public void update(Author author) {
        dmlOperation(AuthorSqlDao::updateInTransaction, author);
    }

    @Override
    public void delete(Integer id) {
        dmlOperation(AuthorSqlDao::deleteInTransaction, id);
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
        while (resultSet.next()) {
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
