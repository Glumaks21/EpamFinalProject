package ua.maksym.hlushchenko.dao.entity;

import java.time.LocalDate;
import java.util.List;

public interface Book {
    int getId();
    void setId(int id);
    String getTitle();
    void setTitle(String title);
    String getDescription();
    void setDescription(String description);
    Author getAuthor();
    void setAuthor(Author author);
    Publisher getPublisher();
    void setPublisher(Publisher publisher);
    LocalDate getDate();
    void setDate(LocalDate date);
    List<Genre> getGenres();
    void setGenres(List<Genre> genres);
    String getCoverPath();
    void setCoverPath(String coverPath);
}
