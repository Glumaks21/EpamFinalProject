package ua.maksym.hlushchenko.orm.dao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.dao.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.orm.exception.SessionException;
import ua.maksym.hlushchenko.orm.entity.EntityParser;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {
    private static SessionImpl session;

    @BeforeAll
    static void init() throws SQLException {
        session = new SessionImpl(HikariCPDataSource.getInstance().getConnection());
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Author.class));
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateQuery() throws SQLException {
        String insertQuery = "INSERT INTO author(name, surname) VALUES('MyFirst', 'Test')";
        String updateQuery = "UPDATE author SET name = 'MySecond', surname = 'Test', alias = 'ForDelete'";
        String deleteQuery = "DELETE FROM author WHERE alias = 'ForDelete'";

        session.update(insertQuery);
        session.update(updateQuery);
        session.update(deleteQuery);
        session.commit();

        String selectQuery = "SELECT id FROM author WHERE name = 'MySecond' AND surname = 'Test'";
        try (ResultSet resultSet = session.query(selectQuery)) {
            assertFalse(resultSet.next());
        }
    }

    @Test
    void rollBack() throws SQLException {
        String firstInsertQuery = "INSERT INTO author(name, surname) VALUES('MyFirst', 'Test')";
        session.update(firstInsertQuery);
        String secondInsertQuery = "INSERT INTO author(name, surname) VALUES('MySecond', 'Test')";
        session.update(secondInsertQuery);

        String errorQuery = "ERROR";
        assertThrows(SessionException.class, () -> session.update(errorQuery));

        String selectQuery = "SELECT id FROM author WHERE name = 'MyFirst' AND surname = 'Test'";
        try (ResultSet resultSet = session.query(selectQuery)) {
            assertFalse(resultSet.next());
        }

        selectQuery = "SELECT id FROM author WHERE name = 'MySecond' AND surname = 'Test'";
        try (ResultSet resultSet = session.query(selectQuery)) {
            assertFalse(resultSet.next());
        }
    }

    @Test
    void query() throws SQLException {
        Object id;
        String insertQuery = "INSERT INTO author(name, surname) VALUES(?, ?)";
        try (ResultSet resultSet = session.updateWithKeys(insertQuery, "My", "Test")) {
            session.commit();

            resultSet.next();
            id = resultSet.getObject(1);
        }

        String selectQuery = "SELECT id, name, surname, alias " +
                "FROM author " +
                "WHERE id = ?";
        try (ResultSet resultSet = session.query(selectQuery, id)) {
            assertTrue(resultSet.next());
            assertDoesNotThrow(() -> resultSet.getObject("id"));
            assertDoesNotThrow(() -> resultSet.getObject("name"));
            assertDoesNotThrow(() -> resultSet.getObject("surname"));
            assertDoesNotThrow(() -> resultSet.getObject("alias"));
        }
    }

    @Test
    void updateQueryWithKeys() throws SQLException {
        String insertQuery = "INSERT INTO author(name, surname) VALUES(?, ?)";

        try (ResultSet resultSet = session.updateWithKeys(insertQuery, "My", "Test")) {
            session.commit();

            assertNotNull(resultSet);
            assertTrue(resultSet.next());
        }
    }

    @AfterAll
    static void destroy() throws SQLException {
        session.closeSession();
    }
}