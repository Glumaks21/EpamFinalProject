package ua.maksym.hlushchenko.dao.entity.sql;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Genre;

import java.util.Objects;

@Data
public class GenreImpl implements Genre {
    private int id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ua.maksym.hlushchenko.dao.entity.Genre)) return false;
        ua.maksym.hlushchenko.dao.entity.Genre genre = (ua.maksym.hlushchenko.dao.entity.Genre) o;
        return getId() == genre.getId() &&
                Objects.equals(getName(), genre.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
