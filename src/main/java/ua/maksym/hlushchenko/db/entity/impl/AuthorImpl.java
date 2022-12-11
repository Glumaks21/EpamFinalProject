package ua.maksym.hlushchenko.db.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Author;

@Data
public class AuthorImpl implements Author {
    private int id;
    private String name;
    private String surname;
}
