package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;

import java.sql.SQLException;

class HikariCPDataSourceTest {

    @Test
    void getConnection() throws SQLException {
        HikariCPDataSource.getInstance().getConnection();
    }
}