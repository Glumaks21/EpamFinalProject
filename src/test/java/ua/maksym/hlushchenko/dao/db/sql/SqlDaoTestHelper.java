package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;

import java.sql.*;
import java.util.*;

public class SqlDaoTestHelper {
    static void clearTables() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT TABLE_NAME " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = 'library_test'");
            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("TABLE_NAME"));
            }

            statement.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
            for (String tableName : tableNames) {
                statement.executeUpdate("DELETE FROM " + tableName);
            }
           statement.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
