package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Publisher;

@Data
public class PublisherImpl implements Publisher {
    private String isbn;
    private String name;
}
