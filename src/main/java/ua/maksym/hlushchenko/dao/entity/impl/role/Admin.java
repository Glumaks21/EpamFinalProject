package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.*;
import ua.maksym.hlushchenko.orm.annotations.*;


@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table("admin")
@IdMappedByColumn("user_id")
public class Admin extends AbstractUser {}
