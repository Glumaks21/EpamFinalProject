package ua.maksym.hlushchenko.dao.db.sql.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumn {
    String value();

    boolean lazyInit() default false;
}
