package ua.maksym.hlushchenko.db.entity.model;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Author;

@Data
public class AuthorModel implements Author {
    private int id;
    private String name;
    private String surname;
}
