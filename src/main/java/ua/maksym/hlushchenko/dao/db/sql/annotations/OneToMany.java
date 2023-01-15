package ua.maksym.hlushchenko.dao.db.sql.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    String mappedBy();

    Class<?> relatedEntity();

    boolean lazyInit() default true;
}
