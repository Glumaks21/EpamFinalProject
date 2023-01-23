package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.entity.impl.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.Admin;
import ua.maksym.hlushchenko.dao.entity.impl.role.Librarian;
import ua.maksym.hlushchenko.dao.entity.impl.role.Reader;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;


public class TestEntityFactory {
    private static final Map<Class<?>, Supplier<?>> map;
    private static final Map<Class<?>, Integer> mappedCounter;
    private final Session session;

    static {
        map = new HashMap<>();
        mappedCounter = new HashMap<>();

        map.put(Author.class, TestEntityFactory::createAuthor);
        map.put(AuthorUa.class, TestEntityFactory::createAuthorUa);
        map.put(Publisher.class, TestEntityFactory::createPublisher);
        map.put(Genre.class, TestEntityFactory::createGenre);
        map.put(GenreUa.class, TestEntityFactory::createGenreUa);
        map.put(Book.class, TestEntityFactory::createBook);
        map.put(Admin.class, TestEntityFactory::createAdmin);
        map.put(Librarian.class, TestEntityFactory::createLibrarian);
        map.put(Reader.class, TestEntityFactory::createReader);
        map.put(Receipt.class, TestEntityFactory::createReceipt);
        map.put(Subscription.class, TestEntityFactory::createSubscription);
    }

    public TestEntityFactory(Session session) {
        this.session = session;
    }

    public static  <T> T createEntity(Class<T> clazz) {
        Supplier<?> supplier = map.get(clazz);
        if (supplier == null) {
            throw new IllegalArgumentException("Class is undefined: " + clazz);
        }
        mappedCounter.merge(clazz, 1, Integer::sum);
        return (T) supplier.get();
    }

    private static Author createAuthor() {
        int count = mappedCounter.get(Author.class);
        Author author = new Author();
        author.setName("Great_" + count);
        author.setSurname("Author_" + count);
        return author;
    }

    private static AuthorUa createAuthorUa() {
        int count = mappedCounter.get(AuthorUa.class);
        AuthorUa author = new AuthorUa();
        author.setName("Мій_" + count);
        author.setSurname("Автор_" + count);
        return author;
    }

    private static Genre createGenre() {
        int count = mappedCounter.get(Genre.class);
        Genre genre = new Genre();
        genre.setName("Genre_" + count);
        return genre;
    }

    private static GenreUa createGenreUa() {
        int count = mappedCounter.get(GenreUa.class);
        GenreUa genre = new GenreUa();
        genre.setName("Жанр_" + count);
        return genre;
    }

    private static Publisher createPublisher() {
        int count = mappedCounter.get(Publisher.class);
        Publisher publisher = new Publisher();
        publisher.setName("Publisher_" + count);
        return publisher;
    }

    private static Book createBook() {
        int count = mappedCounter.get(Book.class);
        Book book = new Book();
        book.setTitle("Book_" + count);
        book.setDate(LocalDate.now());
        book.setDescription("Wonderful book_" + count);
        return book;
    }

    private static Admin createAdmin() {
        int count = mappedCounter.get(Admin.class);
        Admin admin = new Admin();
        admin.setLogin("login_" + count);
        admin.setPasswordHash("1234567890_" + count);
        return admin;
    }

    private static Librarian createLibrarian() {
        int count = mappedCounter.get(Librarian.class);
        Librarian librarian = new Librarian();
        librarian.setLogin("login_" + count);
        librarian.setPasswordHash("1234567890_" + count);
        return librarian;
    }

    private static Reader createReader() {
        int count = mappedCounter.get(Reader.class);
        Reader reader = new Reader();
        reader.setLogin("login_" + count);
        reader.setPasswordHash("1234567890_" + count);
        return reader;
    }

    private static Receipt createReceipt() {
        Receipt receipt = new Receipt();
        receipt.setDateTime(LocalDateTime.now());
        return receipt;
    }

    private static Subscription createSubscription() {
        Subscription subscription = new Subscription();
        subscription.setFine(777);
        subscription.setTakenDate(LocalDate.now().minusDays(777));
        subscription.setBroughtDate(LocalDate.now());
        return subscription;
    }

    public void setSavedValue(Class<?> clazz, Object entity) {
        Class<?> entityClass = entity.getClass();
        for (Field field : entityClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();

            if (fieldType == clazz) {
                Object value = createEntity(fieldType);
                Dao<Object, Object> dao = new GenericDao<>(fieldType, session);
                dao.save(value);
                session.commit();
                EntityParser.setValueTo(field, value, entity);
            }
        }
    }
}
