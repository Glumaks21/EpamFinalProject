package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import java.sql.Connection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorUaSqlDaoTest extends SqlDaoTestHelper {
    private static Connection connection;
    private static AuthorUaSqlDao dao;
    private static Author author;

    static Author createAuthor() {
        AuthorImpl author = new AuthorImpl();
        author.setName("Барак");
        author.setSurname("Обама");
        return author;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();

        AuthorSqlDao daoOriginal = new AuthorEnSqlDao(connection);
        Author authorOriginal = AuthorEnSqlDaoTest.createAuthor();
        daoOriginal.save(authorOriginal);

        dao = new AuthorUaSqlDao(connection);
        author = createAuthor();
        author.setId(authorOriginal.getId());
    }

    @Order(1)
    @Test
    void save() {
        dao.save(author);
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
        Assertions.assertTrue(optionalAuthorInDb.isPresent());
        Author authorInDb = optionalAuthorInDb.get();
        Assertions.assertEquals(author, authorInDb);
    }

    @Order(4)
    @Test
    void update() {
        author.setName("Джо");
        author.setSurname("Біден");
        dao.update(author);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(author.getId());
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        Assertions.assertTrue(optionalAuthorInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        clearTables();
        connection.close();
    }
}