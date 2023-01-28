package ua.maksym.hlushchenko.orm.entity;

import ua.maksym.hlushchenko.dao.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.orm.dao.Dao;
import ua.maksym.hlushchenko.orm.dao.ObjectDao;
import ua.maksym.hlushchenko.orm.exception.ConnectionException;
import ua.maksym.hlushchenko.orm.exception.EntityNotFoundException;
import ua.maksym.hlushchenko.orm.dao.GenericDao;
import ua.maksym.hlushchenko.orm.dao.SessionImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class EntityDaoFactory {
    private final EntityManager entityManager;

    public EntityDaoFactory(EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    public <K, T> Dao<K, T> getDao(Class<T> entityClass) {
        if (!entityManager.isContainsEntity(entityClass)) {
            throw new EntityNotFoundException("Entity of class " + entityClass + " is not scanned");
        }

        try {
            SessionImpl newSession = new SessionImpl(HikariCPDataSource.getInstance().getConnection());
            return new GenericDao<>(entityClass, newSession);
        } catch (SQLException e) {
            throw new ConnectionException(e);
        }
    }
}

class Test {
    public static void main(String[] args) throws SQLException {
        Author author = new Author();
        author.setName("Name");
        author.setSurname("Surname");

        Publisher publisher = new Publisher();
        publisher.setName("Publisher");

        Genre genre1 = new Genre();
        genre1.setName("Genre1");

        Genre genre2 = new Genre();
        genre2.setName("Genre2");

        Reader reader = new Reader();
        reader.setLogin("Login");
        reader.setPasswordHash("123");
        reader.setBlocked(true);

        Receipt receipt1 = new Receipt();
        receipt1.setReader(reader);
        receipt1.setDateTime(LocalDateTime.now());

        Receipt receipt2 = new Receipt();
        receipt2.setReader(reader);
        receipt2.setDateTime(LocalDateTime.now());

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setDescription("Description");
        book.setDate(LocalDate.now());
        book.setGenres(List.of(genre1, genre2));
        book.setReceipts(List.of(receipt1, receipt2));

        receipt1.setBooks(List.of(book));
        receipt2.setBooks(List.of(book));

        SessionImpl session = new SessionImpl(HikariCPDataSource.getInstance().getConnection());
        ObjectDao<Integer, Book> dao = new GenericDao<>(Book.class, session);


        dao.save(book);
        System.out.println(dao.findAll());

        Book fromDb = dao.find(book.getId()).get();
        System.out.println(fromDb);

        System.out.println(fromDb.getGenres());
        System.out.println(fromDb.getReceipts());
        fromDb.getReceipts().forEach(
                receipt -> System.out.println(receipt.getBooks()));

        fromDb.setTitle("Changed");
        fromDb.getGenres().get(0).setName("Changed");
        dao.update(fromDb);


        System.out.println("------------------------------------");

        fromDb = dao.find(book.getId()).get();

        System.out.println(dao.findAll());
        System.out.println(fromDb);

        System.out.println(fromDb.getGenres());
        System.out.println(fromDb.getReceipts());
        fromDb.getReceipts().forEach(
                receipt -> System.out.println(receipt.getBooks()));

        dao.remove(fromDb);

        System.out.println("------------------------------------");

        Optional<Book> optionalBook = dao.find(book.getId());
        System.out.println(optionalBook);

        session.commit();
    }
}
