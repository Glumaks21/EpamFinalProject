package ua.maksym.hlushchenko.dao.entity;

import lombok.*;
import ua.maksym.hlushchenko.orm.entity.annotations.*;


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
