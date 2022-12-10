package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.Book;
import ua.maksym.hlushchenko.db.entity.Genre;

import java.util.List;
import java.util.Set;

public interface BookDao {
    List<Genre> findGenres(Integer id);
    void saveGenres(Book book);
    void updateGenres(Book book);
    void deleteGenres(Integer id);
}
