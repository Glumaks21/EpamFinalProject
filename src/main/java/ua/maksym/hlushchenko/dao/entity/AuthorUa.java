package ua.maksym.hlushchenko.dao.entity;

import lombok.*;
import ua.maksym.hlushchenko.orm.entity.annotations.*;


@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"name", "surname", "alias"}, callSuper = true)
@Entity
@Table("author_ua")
@IdMappedByColumn("author_id")
public class AuthorUa extends Author {
    @Column("name")
    private String name;

    @Column("surname")
    private String surname;

    @Column("alias")
    private String alias;
}
