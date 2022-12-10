package ua.maksym.hlushchenko.db.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface Book {
    int getId();
    void setId(int id);
    String getTitle();
    void setTitle(String title);
    Author getAuthor();
    void setAuthor(Author author);
    Publisher getPublisher();
    void setPublisher(Publisher publisher);
    LocalDate getDate();
    void setDate(LocalDate date);
    List<Genre> getGenres();
    void setGenres(List<Genre> genres);
}
