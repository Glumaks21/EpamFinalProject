package ua.maksym.hlushchenko.orm.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    Class<?> genericType();

    String mappedBy();

    boolean lazyInit() default true;
}
