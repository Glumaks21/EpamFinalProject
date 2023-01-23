package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.exception.SessionException;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {
    private static Session session;

    @BeforeAll
    static void init() throws SQLException {
        Connection connection = HikariCPDataSource.getInstance().getConnection();
        session = new Session(connection);
        SqlDaoTestHelper.clearTables();
    }

    @Test
    void updateQuery() throws SQLException {
        String insertQuery = "INSERT INTO author(name, surname) VALUES('MyFirst', 'Test')";
        String updateQuery = "UPDATE author SET name = 'MySecond', surname = 'Test', alias = 'ForDelete'";
        String deleteQuery = "DELETE FROM author WHERE alias = 'ForDelete'";

        session.updateQuery(insertQuery);
        session.updateQuery(updateQuery);
        session.updateQuery(deleteQuery);
        session.commit();

        String selectQuery = "SELECT id FROM author WHERE name = 'MySecond' AND surname = 'Test'";
        try (ResultSet resultSet = session.query(selectQuery)) {
            assertFalse(resultSet.next());
        }
    }

    @Test
    void rollBack() throws SQLException {
        String firstInsertQuery = "INSERT INTO author(name, surname) VALUES('MyFirst', 'Test')";
        session.updateQuery(firstInsertQuery);
        String secondInsertQuery = "INSERT INTO author(name, surname) VALUES('MySecond', 'Test')";
        session.updateQuery(secondInsertQuery);

        String errorQuery = "ERROR";
        assertThrows(SessionException.class, () -> session.updateQuery(errorQuery));

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
        try (ResultSet resultSet = session.updateQueryWithKeys(insertQuery, "My", "Test")) {
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

        try (ResultSet resultSet = session.updateQueryWithKeys(insertQuery, "My", "Test")) {
            session.commit();

            assertNotNull(resultSet);
            assertTrue(resultSet.next());
        }
    }

    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        session.closeSession();
    }
}