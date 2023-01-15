package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Genre;

import java.util.Objects;

@Data
public class GenreUaImpl implements Genre {
    public static final String SQL_TABLE_NAME = "genre_ua";
    public static final String SQL_COLUMN_NAME_ID = "genre_id";
    public static final String SQL_COLUMN_NAME_NAME = "name";

    private int id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenreUaImpl)) return false;
        GenreUaImpl genre = (GenreUaImpl) o;
        return getId() == genre.getId() &&
                Objects.equals(getName(), genre.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
