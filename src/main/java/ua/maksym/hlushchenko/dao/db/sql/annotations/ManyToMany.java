package ua.maksym.hlushchenko.dao.db.sql.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
    JoinTable joinTable() default @JoinTable(name = "not_defined",
            joinColumn = "not_defined", inverseColumn = "not_defined");

    Class<?> relatedEntity();

    String mappedBy() default "";

    boolean lazyInit() default true;
}
