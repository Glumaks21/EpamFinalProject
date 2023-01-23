package ua.maksym.hlushchenko.orm.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    Class<?> genericType();

    String mappedBy();

    CascadeType[] cascadeType();

    boolean lazyInit() default true;
}
