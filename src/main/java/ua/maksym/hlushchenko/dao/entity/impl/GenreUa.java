package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.*;
import ua.maksym.hlushchenko.orm.annotations.*;


@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "name", callSuper = true)
@Entity
@Table("genre_ua")
@IdMappedByColumn("genre_id")
public class GenreUa extends Genre {
    @Column("name")
    private String name;
}
