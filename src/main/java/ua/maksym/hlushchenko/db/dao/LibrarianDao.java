package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.roles.Librarian;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibrarianDao extends AbstractSqlDao<String, Librarian> {
    private static final String SQL_SELECT_ALL = "SELECT * FROM librarian l " +
            "JOIN user u ON l.user_login = u.login";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM librarian l " +
            "JOIN user u ON l.user_login = u.login WHERE login = ?";
    private static final String SQL_INSERT = "INSERT INTO librarian(user_login) VALUES(?)";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM librarian WHERE user_login = ?";

    public LibrarianDao(Connection connection) {
        super(connection);
    }

    static Librarian mapToLibrarian(ResultSet resultSet) throws SQLException {
        Librarian librarian = new Librarian();
        librarian.setUser(UserDao.mapToUser(resultSet));
        return librarian;
    }

    @Override
    public List<Librarian> findAll() {
        List<Librarian> librarians = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                Librarian librarian = mapToLibrarian(resultSet);
                librarians.add(librarian);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return librarians;
    }

    @Override
    public Optional<Librarian> find(String id) {
        Librarian librarian = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                librarian = mapToLibrarian(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(librarian);
    }

    @Override
    public void save(Librarian librarian) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(UserDao.SQL_INSERT);
            fillPreparedStatement(statement,
                    librarian.getUser().getLogin(),
                    librarian.getUser().getPassword());
            statement.executeUpdate();

            statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement, librarian.getUser().getLogin());
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
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
            statement.executeUpdate();

            statement = connection.prepareStatement(UserDao.SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }
}
