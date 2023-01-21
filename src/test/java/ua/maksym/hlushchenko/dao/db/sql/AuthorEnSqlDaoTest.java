package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import java.sql.Connection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorEnSqlDaoTest {
    private static Connection connection;
    private static Dao<Integer, Author> dao;
    private static Author author;

    static Author createAuthor() {
        AuthorImpl author = new AuthorImpl();
        author.setName("Barak");
        author.setSurname("Obama");
        return author;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new GenericDao<>(AuthorImpl.class, connection);
        author = createAuthor();
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(author);
        assertTrue(author.getId() > 0);
    }

    @Order(2)
    @Test2
    void findAll() {
        List<Author> authors = dao.findAll();
        assertTrue(authors.contains(author));
    }

    @Order(3)
    @Test2
    void find() {
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        assertTrue(optionalAuthorInDb.isPresent());
        Author authorInDb = optionalAuthorInDb.get();
        assertEquals(author, authorInDb);
    }

    @Order(4)
    @Test2
    void update() {
        author.setName("Joe");
        author.setSurname("Biden");
        dao.update(author);
        find();
    }

    @Order(5)
    @Test2
    void delete() {
        dao.delete(author.getId());
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        assertTrue(optionalAuthorInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}