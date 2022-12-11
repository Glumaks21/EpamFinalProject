package ua.maksym.hlushchenko.db.dao.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class HikariCPDataSource {
    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        Properties properties = new Properties();

        System.out.println(new File("").getAbsolutePath());
        try (InputStream is = HikariCPDataSource.class.getClassLoader().getResourceAsStream("db.properties")) {
            properties.load(is);

            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.user"));
            config.setPassword(properties.getProperty("db.password"));
            ds = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HikariCPDataSource(){}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
