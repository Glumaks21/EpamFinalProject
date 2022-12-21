package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Publisher;

import java.util.Objects;

@Data
public class PublisherImpl implements Publisher {
    private String isbn;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Publisher)) return false;
        Publisher publisher = (Publisher) o;
        return Objects.equals(getIsbn(), publisher.getIsbn()) &&
                Objects.equals(getName(), publisher.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIsbn(), getName());
    }
}
