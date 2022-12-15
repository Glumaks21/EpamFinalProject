package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import javax.sql.DataSource;
import java.sql.*;
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
        updateInTransaction(GenreSqlDao::saveInTransaction, genre);
    }

    @Override
    public void update(Genre genre) {
        updateInTransaction(GenreSqlDao::updateInTransaction, genre);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(GenreSqlDao::deleteInTransaction, id);
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
