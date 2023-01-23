package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.*;
import ua.maksym.hlushchenko.orm.annotations.Id;
import ua.maksym.hlushchenko.orm.annotations.IdMappedByColumn;
import ua.maksym.hlushchenko.orm.annotations.Table;


@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("librarian")
@IdMappedByColumn("user_id")
public class Librarian extends AbstractUser {}
