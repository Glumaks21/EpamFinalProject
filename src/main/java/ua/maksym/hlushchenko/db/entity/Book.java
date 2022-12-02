package ua.maksym.hlushchenko.db.entity;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class Book {
    private int id;
    private Author author;
    private Publisher publisher;
    private LocalDate date;
    private String title;

    private List<Genre> genres;
}
