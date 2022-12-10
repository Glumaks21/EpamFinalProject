package ua.maksym.hlushchenko.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ua.maksym.hlushchenko.db.dao.BookDao;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = "dao")
@ToString(exclude = "dao")
public class Book {
    private int id;
    private Author author;
    private Publisher publisher;
    private LocalDate date;
    private String title;

    private BookDao dao;
    private Set<Genre> genres;

    public Set<Genre> getGenres() {
        if (genres == null && dao != null) {
            genres = dao.findGenres(id);
        } else if (genres == null) {
            genres = new HashSet<>();
        }

        return new HashSet<>(genres);
    }
}
