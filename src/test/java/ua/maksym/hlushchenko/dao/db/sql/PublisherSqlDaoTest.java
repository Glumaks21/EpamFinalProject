package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.Publisher;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublisherSqlDaoTest {
    private static Connection connection;
    private static Dao<Integer, Publisher> dao;
    private static Publisher publisher;

    static Publisher createPublisher() {
        Publisher publisher = new Publisher();
        publisher.setName("Test company");
        return publisher;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new GenericDao<>(Publisher.class, connection);
        publisher = createPublisher();
    }


    @Order(1)
    @Test2
    void save() {
        dao.save(publisher);
    }

    @Order(2)
    @Test2
    void findAll() {
        List<Publisher> publishers = dao.findAll();
        Assertions.assertTrue(publishers.contains(publisher));
    }

    @Order(3)
    @Test2
    void find() {
        Optional<Publisher> optionalPublisherInDb = dao.find(publisher.getId());
        Assertions.assertTrue(optionalPublisherInDb.isPresent());
        Publisher publisherInDb = optionalPublisherInDb.get();
        Assertions.assertEquals(publisher, publisherInDb);
    }

    @Order(4)
    @Test2
    void findByName() {
//        Optional<Publisher> optionalPublisherInDb = dao.findByName(publisher.getName());
//        Assertions.assertTrue(optionalPublisherInDb.isPresent());
//        Publisher publisherInDb = optionalPublisherInDb.get();
//        Assertions.assertEquals(publisher, publisherInDb);
    }

    @Order(5)
    @Test2
    void delete() {
        dao.delete(publisher.getId());
        Optional<Publisher> optionalPublisherInDb = dao.find(publisher.getId());
        Assertions.assertTrue(optionalPublisherInDb.isEmpty());
    }

    @Order(6)
    @Test2
    void deleteByName() {
//        save();
//        dao.deleteByName(publisher.getName());
//        Optional<Publisher> optionalPublisherInDb = dao.find(publisher.getId());
//        Assertions.assertTrue(optionalPublisherInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}