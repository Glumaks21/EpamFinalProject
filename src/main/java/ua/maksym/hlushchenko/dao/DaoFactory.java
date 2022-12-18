package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.*;

import java.util.Locale;

public interface DaoFactory {
    Dao<Integer, Role> createRoleDao();
    Dao<Integer, User> createUserDao();
    ReaderDao<Integer>  createReaderDao();
    Dao<Integer, Author> createAuthorDao(Locale locale);
    PublisherDao<String> createPublisherDao();
    Dao<Integer, Genre> createGenreDao(Locale locale);
    BookDao<Integer> createBookDao(Locale locale);
    SubscriptionDao<Integer> createSubscriptionDao();
    ReceiptDao<Integer> createReceiptDao();
}
