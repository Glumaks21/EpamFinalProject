package ua.maksym.hlushchenko.db.dao.sql;

import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.db.dao.sql.HikariCPDataSource;

import java.sql.SQLException;

class HikariCPDataSourceTest {

    @Test
    void getConnection() throws SQLException {
        HikariCPDataSource.getConnection();
    }
}