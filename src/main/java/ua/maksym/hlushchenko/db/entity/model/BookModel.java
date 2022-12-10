package ua.maksym.hlushchenko.db.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ua.maksym.hlushchenko.db.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class BookModel implements Book {
    private int id;
    private Author author;
    private Publisher publisher;
    private LocalDate date;
    private String title;

    private List<Genre> genres;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;

        return getId() == book.getId() &&
                getAuthor().equals(book.getAuthor()) &&
                getPublisher().equals(book.getPublisher()) &&
                getDate().equals(book.getDate()) &&
                getTitle().equals(book.getTitle()) &&
                getGenres().equals(book.getGenres());
    }
}
