package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Author;

import java.util.Objects;

@Data
public class AuthorImpl implements Author {
    private int id;
    private String name;
    private String surname;
    private String alias;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        Author author = (Author) o;
        return getId() == author.getId() &&
                Objects.equals(getName(), author.getName()) &&
                Objects.equals(getSurname(), author.getSurname()) &&
                Objects.equals(getAlias(), author.getAlias());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getSurname(), getAlias());
    }
}
