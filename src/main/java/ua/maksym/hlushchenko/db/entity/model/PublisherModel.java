package ua.maksym.hlushchenko.db.entity.model;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Publisher;

@Data
public class PublisherModel implements Publisher {
    private String isbn;
    private String name;
}
