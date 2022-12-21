package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.*;

import java.util.Locale;

public interface DaoFactory extends AutoCloseable {
    Dao<Integer, Role> createRoleDao();
    UserDao createUserDao();
    ReaderDao  createReaderDao();
    Dao<Integer, Author> createAuthorDao(Locale locale);
    PublisherDao createPublisherDao();
    Dao<Integer, Genre> createGenreDao(Locale locale);
    BookDao createBookDao(Locale locale);
    SubscriptionDao createSubscriptionDao();
    ReceiptDao createReceiptDao();
}
