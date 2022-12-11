package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BookImpl implements Book {
    private int id;
    private String title;
    private Author author;
    private Publisher publisher;
    private LocalDate date;

    private List<Genre> genres = new ArrayList<>();

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
