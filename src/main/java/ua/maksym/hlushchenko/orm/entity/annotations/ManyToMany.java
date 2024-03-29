package ua.maksym.hlushchenko.orm.entity.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
    Class<?> genericType();

    String mappedBy() default "";

    CascadeType[] cascadeTypes();

    boolean lazyInit() default true;
}
