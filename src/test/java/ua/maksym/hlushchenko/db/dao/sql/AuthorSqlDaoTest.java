package ua.maksym.hlushchenko.db.dao.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.Author;
import ua.maksym.hlushchenko.db.entity.model.AuthorModel;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorSqlDaoTest {
    private static Connection connection;
    private static AuthorSqlDao dao;
    private static AuthorModel author;

    static AuthorModel createAuthor() {
        AuthorModel author = new AuthorModel();
        author.setName("Barak");
        author.setSurname("Obama");
        return author;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new AuthorSqlDao(connection);
        author = createAuthor();
    }


    @Order(1)
    @Test
    void save() {
        dao.save(author);
        Assertions.assertTrue(author.getId() > 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Author> authors = dao.findAll();
        Assertions.assertTrue(authors.contains(author));
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
        Assertions.assertTrue(optionalAuthorInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        connection.close();
    }
}