package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.sql.*;

public abstract class GenreSqlDao extends AbstractSqlDao<Integer, Genre> {
    public GenreSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected GenreImpl mapToEntity(ResultSet resultSet) {
        try {
            GenreImpl genre = new GenreImpl();
            genre.setId(resultSet.getInt("id"));
            genre.setName(resultSet.getString("name"));
            return genre;
        } catch (SQLException e) {
            throw new MappingException(e);
        }
    }
}
