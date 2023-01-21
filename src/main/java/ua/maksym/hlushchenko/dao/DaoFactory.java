package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.impl.Author;
import ua.maksym.hlushchenko.dao.entity.impl.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.role.Admin;
import ua.maksym.hlushchenko.dao.entity.impl.role.Librarian;

import java.util.Locale;

public interface DaoFactory {
    UserDao createUserDao();
    ReaderDao createReaderDao();
    Dao<Integer, Librarian> createLibrarianDao();
    Dao<Integer, Admin> createAdminDao();
    Dao<Integer, Author> createAuthorDao(Locale locale);
    PublisherDao createPublisherDao();
    Dao<Integer, Genre> createGenreDao(Locale locale);
    BookDao createBookDao(Locale locale);
    SubscriptionDao createSubscriptionDao();
    ReceiptDao createReceiptDao();
}
