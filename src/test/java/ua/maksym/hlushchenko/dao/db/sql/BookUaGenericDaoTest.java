package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ua.maksym.hlushchenko.dao.db.sql.TestEntityFactory.createEntity;


public class BookUaGenericDaoTest {
    private static Session session;
    private static Dao<Integer, BookUa> dao;

    @BeforeAll
    static void init() throws SQLException {
        session = new Session(HikariCPDataSource.getInstance().getConnection());
        dao = new GenericDao<>(BookUa.class, session);
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Author.class));
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Publisher.class));
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Book.class));
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(AuthorUa.class));
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void find()  {
        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
        BookUa book = createEntity(BookUa.class);
        testEntityFactory.setSavedValue(Publisher.class, book);
        testEntityFactory.setSavedValue(AuthorUa.class, book);

        dao.save(book);
        session.commit();

        assertTrue(book.getId() != 0);

        Optional<BookUa> fromDb = dao.find(book.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(book, fromDb.get());
    }

    @Test
    void findAll() {
//        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
//        BookUa book1 = createEntity(Book.class);
//        testEntityFactory.setSavedValue(Publisher.class, book1);
//        testEntityFactory.setSavedValue(Author.class, book1);
//        Book book2 = createEntity(Book.class);
//        testEntityFactory.setSavedValue(Publisher.class, book2);
//        testEntityFactory.setSavedValue(Author.class, book2);
//
//        dao.save(book1);
//        dao.save(book2);
//        session.commit();
//
//        List<Book> fromDb = dao.findAll();
//        assertTrue(fromDb.contains(book1));
//        assertTrue(fromDb.contains(book2));
    }

    @Test
    void save() {
//        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
//        Book book = createEntity(Book.class);
//        testEntityFactory.setSavedValue(Publisher.class, book);
//        testEntityFactory.setSavedValue(Author.class, book);
//
//        dao.save(book);
//        session.commit();
//
//        assertTrue(book.getId() != 0);
    }

    @Test
    void update() {
//        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
//        Book book = createEntity(Book.class);
//        testEntityFactory.setSavedValue(Publisher.class, book);
//        testEntityFactory.setSavedValue(Author.class, book);
//
//        dao.save(book);
//        session.commit();
//
//        book = dao.find(book.getId()).get();
//        book.setTitle("TestSuccessfully");
//        dao.update(book);
//        session.commit();
//
//        book = dao.find(book.getId()).get();
//        assertEquals("TestSuccessfully", book.getTitle());
    }

    @Test
    void delete() {
//        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
//        Book book = createEntity(Book.class);
//        testEntityFactory.setSavedValue(Publisher.class, book);
//        testEntityFactory.setSavedValue(Author.class, book);
//
//        dao.save(book);
//        dao.delete(book.getId());
//        session.commit();
//
//        Optional<Book> fromDb = dao.find(book.getId());
//        assertTrue(fromDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        session.closeSession();
    }
}
