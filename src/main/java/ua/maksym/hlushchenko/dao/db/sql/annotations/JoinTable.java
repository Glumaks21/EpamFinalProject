package ua.maksym.hlushchenko.dao.db.sql.annotations;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {
    String name();

    String joinColumn();

    String inverseColumn();
}