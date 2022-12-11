package ua.maksym.hlushchenko.db.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Publisher;

@Data
public class PublisherImpl implements Publisher {
    private String isbn;
    private String name;
}
