package ua.maksym.hlushchenko.orm.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
    Class<?> genericType();

    String mappedBy() default "";

    boolean lazyInit() default true;
}
