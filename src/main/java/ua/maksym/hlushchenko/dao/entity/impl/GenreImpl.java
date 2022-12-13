package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Genre;

@Data
public class GenreImpl implements Genre {
    private int id;
    private String name;
}
