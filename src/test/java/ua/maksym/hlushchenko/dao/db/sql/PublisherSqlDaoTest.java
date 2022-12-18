package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Publisher;
import ua.maksym.hlushchenko.dao.entity.impl.PublisherImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublisherSqlDaoTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static PublisherSqlDao dao;
    private static Publisher publisher;

    static Publisher createPublisher() {
        Publisher publisher = new PublisherImpl();
        publisher.setIsbn("1234567890123");
        publisher.setName("Test company");
        return publisher;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        setUpTables();
        sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createPublisherDao();
        publisher = createPublisher();
    }

    @SneakyThrows
    static void setUpTables() {
        String dropQuery = "DROP TABLE IF EXISTS `publisher`";
        String createQuery = "CREATE TABLE `publisher` (\n" +
                "                             `isbn` varchar(17) NOT NULL,\n" +
                "                             `name` varchar(45) NOT NULL,\n" +
                "                             PRIMARY KEY (`isbn`),\n" +
                "                             UNIQUE KEY `name_UNIQUE` (`name`),\n" +
                "                             UNIQUE KEY `isbn_UNIQUE` (`isbn`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
        statement.executeUpdate(createQuery);
    }

    @Order(1)
    @Test
    void save() {
        dao.save(publisher);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Publisher> publishers = dao.findAll();
        Assertions.assertTrue(publishers.contains(publisher));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Publisher> optionalPublisherInDb = dao.find(publisher.getIsbn());
        Assertions.assertTrue(optionalPublisherInDb.isPresent());
        Publisher publisherInDb = optionalPublisherInDb.get();
        Assertions.assertEquals(publisher, publisherInDb);
    }

    @Order(4)
    @Test
    void findByName() {
        Optional<Publisher> optionalPublisherInDb = dao.findByName(publisher.getName());
        Assertions.assertTrue(optionalPublisherInDb.isPresent());
        Publisher publisherInDb = optionalPublisherInDb.get();
        Assertions.assertEquals(publisher, publisherInDb);
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(publisher.getIsbn());
        Optional<Publisher> optionalPublisherInDb = dao.find(publisher.getIsbn());
        Assertions.assertTrue(optionalPublisherInDb.isEmpty());
    }

    @Order(6)
    @Test
    void deleteByName() {
        save();
        dao.deleteByName(publisher.getName());
        Optional<Publisher> optionalPublisherInDb = dao.find(publisher.getIsbn());
        Assertions.assertTrue(optionalPublisherInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        dropTables();
        sqlDaoFactory.close();
    }

    @SneakyThrows
    static void dropTables() {
        String dropQuery = "DROP TABLE `publisher`";
        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
    }
}