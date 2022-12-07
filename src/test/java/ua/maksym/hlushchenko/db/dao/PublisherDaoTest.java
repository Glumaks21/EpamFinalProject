package ua.maksym.hlushchenko.db.dao;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.dao.sql.PublisherSqlDao;
import ua.maksym.hlushchenko.db.entity.Publisher;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublisherDaoTest {
    private static Connection connection;
    private static PublisherDao dao;
    private static Publisher publisher;

    static Publisher createPublisher() {
        Publisher publisher = new Publisher();
        publisher.setIsbn("1234567890123");
        publisher.setName("Test company");
        return publisher;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new PublisherSqlDao(connection);
        publisher = createPublisher();
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
        connection.close();
    }
}