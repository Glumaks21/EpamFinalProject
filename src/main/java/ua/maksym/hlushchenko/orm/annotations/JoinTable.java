package ua.maksym.hlushchenko.orm.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {
    String name();

    String joinColumn();

    String inverseColumn();
}