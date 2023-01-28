package ua.maksym.hlushchenko.dao.entity.role;

import lombok.*;
import ua.maksym.hlushchenko.orm.entity.annotations.*;



@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table("admin")
@IdMappedByColumn("user_id")
public class Admin extends AbstractUser {}
