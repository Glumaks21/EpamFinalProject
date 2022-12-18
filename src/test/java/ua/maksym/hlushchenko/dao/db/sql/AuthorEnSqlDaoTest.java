package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import java.sql.*;
import java.util.*;

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

    @SneakyThrows
    @BeforeAll
    static void init() {
        setUpTables();
        sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createAuthorDao(Locale.ENGLISH);
        author = createAuthor();
    }

    @SneakyThrows
    static void setUpTables() {
        String dropQuery = "DROP TABLE IF EXISTS `author`";
        String createQuery = "CREATE TABLE `author` (\n" +
                "                          `id` int NOT NULL AUTO_INCREMENT,\n" +
                "                          `name` varchar(45) NOT NULL,\n" +
                "                          `surname` varchar(45) NOT NULL,\n" +
                "                          PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=469 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
        statement.executeUpdate(createQuery);
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
        dropTables();
        sqlDaoFactory.close();
    }

    @SneakyThrows
    static void dropTables() {
        String dropQuery = "DROP TABLE `author`";
        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
    }
}