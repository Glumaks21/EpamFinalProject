package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import javax.sql.DataSource;
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
        updateInTransaction(GenreEnSqlDao::saveInTransaction, genre);
    }

    @Override
    public void update(Genre genre) {
        updateInTransaction(GenreEnSqlDao::updateInTransaction, genre);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(GenreEnSqlDao::deleteInTransaction, id);
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
