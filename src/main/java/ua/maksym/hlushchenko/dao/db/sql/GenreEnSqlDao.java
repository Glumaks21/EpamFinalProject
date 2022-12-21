package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.exception.DaoException;

import java.sql.*;
import java.util.*;

public class GenreEnSqlDao extends GenreSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT id, name " +
            "FROM genre";
    private static final String SQL_SELECT_BY_ID = "SELECT id, name " +
            "FROM genre " +
            "WHERE id = ?";
    private static final String SQL_INSERT = "INSERT INTO genre(name) " +
            "VALUES(?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE genre " +
            "SET name = ? " +
            "WHERE id = ?";
    private  static final String SQL_DELETE_BY_ID = "DELETE FROM genre " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(GenreEnSqlDao.class);

    public GenreEnSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    public List<Genre> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Genre> find(Integer id) {
        List<Genre> genres = mappedQuery(SQL_SELECT_BY_ID, id);
        if (genres.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(genres.get(0));
    }

    @Override
    public void save(Genre genre) {
        try (ResultSet resultSet = updateQuery(SQL_INSERT, genre.getName())) {
            if (resultSet.next()) {
                genre.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Genre genre) {
        updateQuery(SQL_UPDATE_BY_ID,
                genre.getName(),
                genre.getId());
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }
}
