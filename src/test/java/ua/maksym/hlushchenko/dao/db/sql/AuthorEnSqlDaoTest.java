package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorEnSqlDaoTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static AuthorSqlDao dao;
    private static Author author;

    static Author createAuthor() {
        AuthorImpl author = new AuthorImpl();
        author.setName("Barak");
        author.setSurname("Obama");
        return author;
    }

    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createAuthorDao(Locale.ENGLISH);
        author = createAuthor();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(author);
        assertTrue(author.getId() > 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Author> authors = dao.findAll();
        assertTrue(authors.contains(author));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        assertTrue(optionalAuthorInDb.isPresent());
        Author authorInDb = optionalAuthorInDb.get();
        assertEquals(author, authorInDb);
    }

    @Order(4)
    @Test
    void update() {
        author.setName("Joe");
        author.setSurname("Biden");
        dao.update(author);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(author.getId());
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        assertTrue(optionalAuthorInDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        sqlDaoFactory.close();
    }
}