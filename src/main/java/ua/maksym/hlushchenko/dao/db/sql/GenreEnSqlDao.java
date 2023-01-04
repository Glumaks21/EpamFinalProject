package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

class GenreEnSqlDao extends AbstractSqlDao<Integer, Genre> {
    static final String SQL_TABLE_NAME = "genre";
    static final String SQL_COLUMN_NAME_ID = "id";
    static final String SQL_COLUMN_NAME_NAME = "name";


    private static final String SQL_SELECT_ALL = QueryUtil.createSelect(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME));

    private static final String SQL_SELECT_BY_ID = QueryUtil.createSelectWithConditions(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME),
            List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_INSERT =  QueryUtil.createInsert(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_NAME));

    private static final String SQL_UPDATE_BY_ID = QueryUtil.createUpdate(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_NAME),
            List.of(SQL_COLUMN_NAME_ID));

    private  static final String SQL_DELETE_BY_ID = QueryUtil.createDelete(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID));


    private static final Logger log = LoggerFactory.getLogger(GenreEnSqlDao.class);

    public GenreEnSqlDao(Connection connection) {
        super(connection);
    }

    protected Genre mapToEntity(ResultSet resultSet) {
        try {
            int id = extractColumn(resultSet, SQL_TABLE_NAME, SQL_COLUMN_NAME_ID);
            String name = extractColumn(resultSet, SQL_TABLE_NAME, SQL_COLUMN_NAME_NAME);

            Genre genre = new GenreImpl();
            genre.setId(id);
            genre.setName(name);
            return (Genre) Proxy.newProxyInstance(
                    GenreEnSqlDao.class.getClassLoader(),
                    new Class[] {Genre.class, LoadProxy.class},
                    new LoadHandler<>(genre));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
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
        try (ResultSet resultSet = updateQueryWithKeys(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS, genre.getName())) {
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
