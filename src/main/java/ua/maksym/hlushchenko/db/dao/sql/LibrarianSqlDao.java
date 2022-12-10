package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.db.entity.model.role.LibrarianModel;
import ua.maksym.hlushchenko.db.entity.role.Librarian;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public LibrarianSqlDao(Connection connection) {
        super(connection);
    }

    static LibrarianModel mapToLibrarian(ResultSet resultSet) throws SQLException {
        LibrarianModel librarian = new LibrarianModel();
        librarian.setLogin(resultSet.getString("login"));
        librarian.setPassword(resultSet.getString("password"));
        return librarian;
    }

    @Override
    public List<Librarian> findAll() {
        List<Librarian> librarians = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();

            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Librarian librarian = mapToLibrarian(resultSet);
                librarians.add(librarian);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return librarians;
    }

    @Override
    public Optional<Librarian> find(String id) {
        LibrarianModel librarian = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);

            log.info("Try to execute:\n" + formatSql(statement));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                librarian = mapToLibrarian(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }

        return Optional.ofNullable(librarian);
    }

    @Override
    public void save(Librarian librarian) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(UserSqlDao.SQL_INSERT);
            fillPreparedStatement(statement,
                    librarian.getLogin(),
                    librarian.getPassword());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement, librarian.getLogin());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Librarian entity) {}

    @Override
    public void delete(String id) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            statement = connection.prepareStatement(UserSqlDao.SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }
}
