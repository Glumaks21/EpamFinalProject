package ua.maksym.hlushchenko.db.dao.sql;

import ua.maksym.hlushchenko.db.entity.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreDao extends AbstractSqlDao<Integer, Genre> {
    static final String SQL_SELECT_ALL = "SELECT * FROM genre";
    static final String SQL_SELECT_BY_ID = "SELECT * FROM genre WHERE id = ?";
    static final String SQL_INSERT = "INSERT INTO genre(name) VALUES(?)";
    static final String SQL_UPDATE_BY_ID = "UPDATE genre SET name = ? WHERE id = ?";
    static final String SQL_DELETE_BY_ID = "DELETE FROM genre WHERE id = ?";

    public GenreDao(Connection connection) {
        super(connection);
    }

    static Genre mapToGenre(ResultSet resultSet) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("id"));
        genre.setName(resultSet.getString("name"));
        return genre;
    }

    @Override
    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                Genre genre = mapToGenre(resultSet);
                genres.add(genre);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return genres;
    }

    @Override
    public Optional<Genre> find(Integer id) {
        Genre genre = null;

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                genre = mapToGenre(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(genre);
    }

    @Override
    public void save(Genre genre) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                    Statement.RETURN_GENERATED_KEYS);
            fillPreparedStatement(statement, genre.getName());
            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();
            while (resultSet.next()) {
                genre.setId(resultSet.getInt(1));
            }

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Genre genre) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
            fillPreparedStatement(statement,
                    genre.getName(),
                    genre.getId());
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
