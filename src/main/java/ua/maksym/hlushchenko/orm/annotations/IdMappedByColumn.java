package ua.maksym.hlushchenko.orm.annotations;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdMappedByColumn {
    String value();
}
