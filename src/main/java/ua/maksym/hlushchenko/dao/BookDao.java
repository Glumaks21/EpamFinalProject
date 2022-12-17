package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Genre;

import java.util.List;

public interface BookDao<K> extends Dao<K, Book> {
    List<Genre> findGenres(K id);
    void saveGenres(Book book);
    void updateGenres(Book book);
    void deleteGenres(K id);
}
