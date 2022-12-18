package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Genre;

import java.sql.*;
import java.util.*;

public class GenreUaSqlDao extends GenreSqlDao {
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
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Genre> find(Integer id) {
        List<Genre> genres = mappedQueryResult(SQL_SELECT_BY_ID, id);
        if (genres.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(genres.get(0));
    }

    @Override
    public void save(Genre genre) {
        updateInTransaction(GenreUaSqlDao::saveInTransaction, genre);
    }

    @Override
    public void update(Genre genre) {
        updateInTransaction(GenreUaSqlDao::updateInTransaction, genre);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(GenreUaSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Genre genre, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement,
                genre.getId(),
                genre.getName());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
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
