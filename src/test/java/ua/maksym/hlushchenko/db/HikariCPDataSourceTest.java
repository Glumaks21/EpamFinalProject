package ua.maksym.hlushchenko.db;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class HikariCPDataSourceTest {

    @Test
    void getConnection() throws SQLException {
        HikariCPDataSource.getConnection();
    }
}