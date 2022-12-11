package ua.maksym.hlushchenko.db.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Genre;

@Data
public class GenreImpl implements Genre {
    private int id;
    private String name;
}
