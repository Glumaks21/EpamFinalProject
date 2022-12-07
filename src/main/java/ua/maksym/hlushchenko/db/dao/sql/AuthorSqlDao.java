package ua.maksym.hlushchenko.db.dao.sql;

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

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Author author = mapToAuthor(resultSet);
                authors.add(author);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return authors;
    }

    @Override
    public Optional<Author> find(Integer id) {
        Author author = null;

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                author = mapToAuthor(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();
            while (resultSet.next()) {
                author.setId(resultSet.getInt(1));
            }

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
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
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
            fillPreparedStatement(statement, id);
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }
}
