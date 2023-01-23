package ua.maksym.hlushchenko.dao.entity.impl;


import lombok.*;
import ua.maksym.hlushchenko.orm.annotations.*;

import java.util.List;

@Getter
@Setter
@ToString(exclude = {"genresUa"}, callSuper = true)
@EqualsAndHashCode(exclude = {"title", "author", "description",  "genresUa"}, callSuper = true)

@Table("book_ua")
@IdMappedByColumn("book_id")
public class BookUa extends Book {
    @Column("title")
    private String title;

    @ManyToOne(cascadeTypes = {CascadeType.SAVE, CascadeType.UPDATE})
    @JoinColumn("author_id")
    private AuthorUa author;

    @Column("description")
    private String description;

    @ManyToMany(genericType = GenreUa.class,
            cascadeTypes = {CascadeType.SAVE, CascadeType.UPDATE})
    @JoinTable(name = "book_has_genre",
            joinColumn = "book_id",
            inverseColumn = "genre_id")
    private List<GenreUa> genresUa;
}
