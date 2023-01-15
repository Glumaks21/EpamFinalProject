package ua.maksym.hlushchenko.dao.db.sql.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {
    Class<?> relatedEntity();

    String mappedBy() default "";

    boolean lazyInit() default false;
}
