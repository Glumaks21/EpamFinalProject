package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.db.entity.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorSqlDao extends AbstractSqlDao<Integer, Author> {
    static String SQL_SELECT_ALL = "SELECT * FROM author";
    static String SQL_SELECT_BY_ID = "SELECT * FROM author WHERE id = ?";
    static String SQL_INSERT = "INSERT INTO author(name, surname) VALUES(?, ?)";
    static String SQL_UPDATE_BY_ID = "UPDATE author SET name = ?, surname = ? WHERE id = ?";
    static String SQL_DELETE_BY_ID = "DELETE FROM author WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(AuthorSqlDao.class);

    public AuthorSqlDao(Connection connection) {
        super(connection);
    }

    static Author mapToAuthor(ResultSet resultSet) throws SQLException {
        Author author = new Author();
        author.setId(resultSet.getInt("id"));
        author.setName(resultSet.getString("name"));
        author.setSurname(resultSet.getString("surname"));
        return author;
    }

    @Override
    public List<Author> findAll() {
        List<Author> authors = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();

            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Author author = mapToAuthor(resultSet);
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

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                author = mapToAuthor(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(author);
    }

    @Override
    public void save(Author author) {
        try {
            connection.setAutoCommit(false);

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

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack(connection);
        }
    }

    @Override
    public void update(Author author) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
            fillPreparedStatement(statement,
                    author.getName(),
                    author.getSurname(),
                    author.getId());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack(connection);
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack(connection);
        }
    }
}
