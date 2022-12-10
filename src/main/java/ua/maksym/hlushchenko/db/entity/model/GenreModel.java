package ua.maksym.hlushchenko.db.entity.model;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Genre;

@Data
public class GenreModel implements Genre {
    private int id;
    private String name;
}
