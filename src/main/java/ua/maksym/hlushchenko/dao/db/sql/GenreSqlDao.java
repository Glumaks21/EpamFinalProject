package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreSqlDao extends AbstractSqlDao<Integer, Genre> {
    static final String SQL_SELECT_ALL = "SELECT * FROM genre";
    static final String SQL_SELECT_BY_ID = "SELECT * FROM genre " +
            "WHERE id = ?";
    static final String SQL_INSERT = "INSERT INTO genre(name) " +
            "VALUES(?)";
    static final String SQL_UPDATE_BY_ID = "UPDATE genre SET name = ? " +
            "WHERE id = ?";
    static final String SQL_DELETE_BY_ID = "DELETE FROM genre " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(GenreSqlDao.class);

    public GenreSqlDao(Connection connection) {
        super(connection);
    }

    static GenreImpl mapToGenre(ResultSet resultSet) throws SQLException {
        GenreImpl genre = new GenreImpl();
        genre.setId(resultSet.getInt("id"));
        genre.setName(resultSet.getString("name"));
        return genre;
    }

    @Override
    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                GenreImpl genre = mapToGenre(resultSet);
                genres.add(genre);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return genres;
    }

    @Override
    public Optional<Genre> find(Integer id) {
        Genre genre = null;

        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                genre = mapToGenre(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(genre);
    }

    @Override
    public void save(Genre genre) {
        try {
            connection.setAutoCommit(false);
            saveInSession(genre, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Genre genre) {
        try {
            connection.setAutoCommit(false);
            updateInSession(genre, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            connection.setAutoCommit(false);
            deleteInSession(id, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveInSession(Genre genre, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement, genre.getName());

        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        while (resultSet.next()) {
            genre.setId(resultSet.getInt(1));
        }
    }

    static void updateInSession(Genre genre, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                genre.getName(),
                genre.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInSession(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
