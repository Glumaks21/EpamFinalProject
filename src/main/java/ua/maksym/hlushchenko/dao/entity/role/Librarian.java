package ua.maksym.hlushchenko.dao.entity.role;

import lombok.*;
import ua.maksym.hlushchenko.orm.entity.annotations.Entity;
import ua.maksym.hlushchenko.orm.entity.annotations.IdMappedByColumn;
import ua.maksym.hlushchenko.orm.entity.annotations.Table;


@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table("librarian")
@IdMappedByColumn("user_id")
public class Librarian extends AbstractUser {}
