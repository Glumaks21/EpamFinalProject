package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorUa;

import java.sql.Connection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorUaSqlDaoTest {
    private static Connection connection;
    private static Dao<Integer, Author> dao;
    private static Author author;

    static Author createAuthor() {
        Author author = new AuthorUa();
        author.setName("Барак");
        author.setSurname("Обама");
        return author;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();

        Dao<Integer, Author> daoOriginal = new GenericDao<>(AuthorImpl.class, connection);
        Author authorOriginal = AuthorEnSqlDaoTest.createAuthor();
        daoOriginal.save(authorOriginal);

        dao = new GenericDao<>(AuthorUa.class, connection);
        author = createAuthor();
        author.setId(authorOriginal.getId());
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(author);
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
        Assertions.assertTrue(optionalAuthorInDb.isPresent());
        Author authorInDb = optionalAuthorInDb.get();
        Assertions.assertEquals(author, authorInDb);
    }

    @Order(4)
    @Test2
    void update() {
        author.setName("Джо");
        author.setSurname("Біден");
        dao.update(author);
        find();
    }

    @Order(5)
    @Test2
    void delete() {
        dao.delete(author.getId());
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        Assertions.assertTrue(optionalAuthorInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}