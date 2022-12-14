package ua.maksym.hlushchenko.dao.db;

import com.zaxxer.hikari.*;

import java.io.*;
import java.util.Properties;

public class HikariCPDataSource {
    private static final HikariDataSource instance;

    static {
        HikariConfig config = new HikariConfig();
        Properties properties = new Properties();

        System.out.println(new File("").getAbsolutePath());
        try (InputStream is = HikariCPDataSource.class.getClassLoader().
                getResourceAsStream("db.properties")) {
            properties.load(is);

            config.setDriverClassName(properties.getProperty("db.driver"));
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.user"));
            config.setPassword(properties.getProperty("db.password"));
            instance = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HikariCPDataSource(){}

    public static HikariDataSource getInstance() {
        return instance;
    }
}
