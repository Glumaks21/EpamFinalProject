package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Author;

@Data
public class AuthorImpl implements Author {
    private int id;
    private String name;
    private String surname;
}
