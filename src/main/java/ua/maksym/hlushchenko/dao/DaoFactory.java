package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.Role;
import ua.maksym.hlushchenko.dao.entity.role.User;

import java.util.Locale;

public interface DaoFactory {
    <D extends Dao<K, T>, K, T  extends Entity> D createDao(Class<T> entityClass);

    <K> Dao<K, Role> createRoleDao();
    <K> Dao<K, User> createUserDao();
    <K> ReaderDao<K> createReaderDao();
    <K> Dao<K, Author> createAuthorDao(Locale locale);
    <K> PublisherDao<K> createPublisherDao();
    <K> Dao<K, Genre>  createGenreDao(Locale locale);
    <K> BookDao<K> createBookDao(Locale locale);
    <K> SubscriptionDao<K> createSubscriptionDao();
    <K> ReceiptDao<K> createReceiptDao();
}
