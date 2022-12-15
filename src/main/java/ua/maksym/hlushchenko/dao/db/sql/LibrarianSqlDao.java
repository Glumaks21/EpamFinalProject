package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.role.LibrarianImpl;
import ua.maksym.hlushchenko.dao.entity.role.Librarian;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;


public class LibrarianSqlDao extends AbstractSqlDao<String, Librarian> {
    private static final String SQL_SELECT_ALL = "SELECT * FROM librarian l " +
            "JOIN user u ON l.user_login = u.login";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM librarian l " +
            "JOIN user u ON l.user_login = u.login " +
            "WHERE login = ?";
    private static final String SQL_INSERT = "INSERT INTO librarian(user_login) " +
            "VALUES(?)";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM librarian " +
            "WHERE user_login = ?";

    private static final Logger log = LoggerFactory.getLogger(LibrarianSqlDao.class);

    public LibrarianSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected LibrarianImpl mapToEntity(ResultSet resultSet) throws SQLException {
        LibrarianImpl librarian = new LibrarianImpl();
        librarian.setLogin(resultSet.getString("login"));
        librarian.setPassword(resultSet.getString("password"));
        return librarian;
    }

    @Override
    public List<Librarian> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Librarian> find(String login) {
        List<Librarian > librarians = mappedQueryResult(SQL_SELECT_BY_LOGIN, login);
        if (librarians.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(librarians.get(0));
    }

    @Override
    public void save(Librarian librarian) {
        dmlOperation(LibrarianSqlDao::saveInTransaction, librarian);
    }

    @Override
    public void update(Librarian entity) {}

    @Override
    public void delete(String id) {
        dmlOperation(LibrarianSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Librarian librarian, Connection connection) throws SQLException {
        UserSqlDao.saveInTransaction(librarian, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement, librarian.getLogin());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(String login, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, login);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        UserSqlDao.deleteInTransaction(login, connection);
    }
}
