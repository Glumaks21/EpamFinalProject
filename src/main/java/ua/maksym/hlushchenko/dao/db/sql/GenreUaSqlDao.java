package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Genre;

import java.sql.*;
import java.util.*;

class GenreUaSqlDao extends GenreSqlDao {
    private static final String SQL_SELECT_ALL = "SELECT genre_id as id, name " +
            "FROM genre_ua";
    private static final String SQL_SELECT_BY_ID = "SELECT genre_id as id, name " +
            "FROM genre_ua " +
            "WHERE genre_id = ?";
    private static final String SQL_INSERT = "INSERT INTO genre_ua(genre_id, name) " +
            "VALUES(?, ?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE genre_ua " +
            "SET name = ? " +
            "WHERE genre_id = ?";
    private  static final String SQL_DELETE_BY_ID = "DELETE FROM genre_ua " +
            "WHERE genre_id = ?";

    private static final Logger log = LoggerFactory.getLogger(GenreUaSqlDao.class);

    public GenreUaSqlDao(Connection connection) {
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
        updateQuery(SQL_INSERT,
                genre.getId(),
                genre.getName());
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
