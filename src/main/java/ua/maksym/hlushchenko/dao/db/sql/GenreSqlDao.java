package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;
import ua.maksym.hlushchenko.exception.DaoException;

import javax.sql.DataSource;
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

    public GenreSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected GenreImpl mapToEntity(ResultSet resultSet) throws SQLException {
        GenreImpl genre = new GenreImpl();
        genre.setId(resultSet.getInt("id"));
        genre.setName(resultSet.getString("name"));
        return genre;
    }

    @Override
    public List<Genre> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            List<Genre> genres = new ArrayList<>();
            while (resultSet.next()) {
                GenreImpl genre = mapToEntity(resultSet);
                genres.add(genre);
            }

            return genres;
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    @Override
    public Optional<Genre> find(Integer id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            Genre genre = null;
            if (resultSet.next()) {
                genre = mapToEntity(resultSet);
            }

            return Optional.ofNullable(genre);
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    @Override
    public void save(Genre genre) {
        dmlOperation(GenreSqlDao::saveInTransaction, genre);
    }

    @Override
    public void update(Genre genre) {
        dmlOperation(GenreSqlDao::updateInTransaction, genre);
    }

    @Override
    public void delete(Integer id) {
        dmlOperation(GenreSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Genre genre, Connection connection) throws SQLException {
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

    static void updateInTransaction(Genre genre, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement,
                genre.getName(),
                genre.getId());
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
