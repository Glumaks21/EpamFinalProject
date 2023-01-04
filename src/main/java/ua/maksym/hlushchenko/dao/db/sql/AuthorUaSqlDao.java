package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

class AuthorUaSqlDao extends AbstractSqlDao<Integer, Author> {
    static final String SQL_TABLE_NAME = "author_ua";
    static final String SQL_COLUMN_NAME_ID = "author_id";
    static final String SQL_COLUMN_NAME_NAME = "name";
    static final String SQL_COLUMN_NAME_SURNAME = "surname";
    static final String SQL_COLUMN_NAME_ALIAS = "alias";

    private static final String SQL_SELECT_ALL = QueryUtil.createSelect(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME, SQL_COLUMN_NAME_SURNAME, SQL_COLUMN_NAME_ALIAS));

    private static final String SQL_SELECT_BY_ID = QueryUtil.createSelectWithConditions(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME, SQL_COLUMN_NAME_SURNAME, SQL_COLUMN_NAME_ALIAS),
            List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_INSERT = QueryUtil.createInsert(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID, SQL_COLUMN_NAME_NAME, SQL_COLUMN_NAME_SURNAME, SQL_COLUMN_NAME_ALIAS));

    private static final String SQL_UPDATE_BY_ID = QueryUtil.createUpdate(SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_NAME, SQL_COLUMN_NAME_SURNAME, SQL_COLUMN_NAME_ALIAS),
            List.of(SQL_COLUMN_NAME_ID));

    private static final String SQL_DELETE_BY_ID = QueryUtil.createDelete(
            SQL_TABLE_NAME,
            List.of(SQL_COLUMN_NAME_ID));

    private static final Logger log = LoggerFactory.getLogger(AuthorUaSqlDao.class);

    public AuthorUaSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Author mapToEntity(ResultSet resultSet) {
        try {
            int id = extractColumn(resultSet, SQL_TABLE_NAME, SQL_COLUMN_NAME_ID);
            String name = extractColumn(resultSet, SQL_TABLE_NAME, SQL_COLUMN_NAME_NAME);
            String surname = extractColumn(resultSet, SQL_TABLE_NAME, SQL_COLUMN_NAME_SURNAME);
            String alias = extractColumn(resultSet, SQL_TABLE_NAME, SQL_COLUMN_NAME_ALIAS);

            Author author = new AuthorImpl();
            author.setId(id);
            author.setName(name);
            author.setSurname(surname);
            author.setAlias(alias);
            return (Author) Proxy.newProxyInstance(
                    AuthorEnSqlDao.class.getClassLoader(),
                    new Class[]{Author.class, LoadProxy.class},
                    new LoadHandler<>(author));
        } catch (SQLException e) {
            throw new MappingException("Can't map the entity", e);
        }
    }

    @Override
    public List<Author> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Author> find(Integer id) {
        List<Author> authors = mappedQuery(SQL_SELECT_BY_ID, id);
        if (authors.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(authors.get(0));
    }

    @Override
    public void save(Author author) {
        updateQuery(SQL_INSERT,
                author.getId(),
                author.getName(),
                author.getSurname(),
                author.getAlias());
    }

    @Override
    public void update(Author author) {
       updateQuery(SQL_UPDATE_BY_ID,
               author.getName(),
               author.getSurname(),
               author.getAlias(),
               author.getId());
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }
}
