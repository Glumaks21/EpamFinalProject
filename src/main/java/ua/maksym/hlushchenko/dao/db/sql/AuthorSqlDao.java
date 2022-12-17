package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.Author;

import javax.sql.DataSource;

public abstract class AuthorSqlDao extends AbstractSqlDao<Integer, Author> {
    public AuthorSqlDao(DataSource dataSource) {
        super(dataSource);
    }
}
