package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.role.Admin;
import ua.maksym.hlushchenko.dao.entity.role.Librarian;
import ua.maksym.hlushchenko.orm.dao.Dao;

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
