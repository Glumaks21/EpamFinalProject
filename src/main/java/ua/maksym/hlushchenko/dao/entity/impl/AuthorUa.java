package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.*;
import ua.maksym.hlushchenko.orm.annotations.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"name", "surname", "alias"}, callSuper = true)
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
