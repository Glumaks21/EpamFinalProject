package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.impl.Book;
import ua.maksym.hlushchenko.dao.entity.impl.Genre;

import java.util.List;

public interface BookDao extends Dao<Integer, Book> {
    List<Genre> findGenres(Integer id);
    void saveGenres(Book book);
    void updateGenres(Book book);
    void deleteGenres(Integer id);
}
