package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.Genre;

import javax.sql.DataSource;

public abstract class GenreSqlDao extends AbstractSqlDao<Integer, Genre> {
    public GenreSqlDao(DataSource dataSource) {
        super(dataSource);
    }
}
