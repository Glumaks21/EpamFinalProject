package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;

abstract class GenreSqlDao extends AbstractSqlDao<Integer, Genre> {
    public GenreSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Genre mapToEntity(ResultSet resultSet) {
        try {
            Genre genre = new GenreImpl();
            genre.setId(resultSet.getInt("id"));
            genre.setName(resultSet.getString("name"));
            return (Genre) Proxy.newProxyInstance(
                    GenreSqlDao.class.getClassLoader(),
                    new Class[] {Genre.class, LoadProxy.class},
                    new LoadHandler<>(genre));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }
}
