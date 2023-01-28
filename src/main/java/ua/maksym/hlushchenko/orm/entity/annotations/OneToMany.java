package ua.maksym.hlushchenko.orm.entity.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    Class<?> genericType();

    String mappedBy();

    CascadeType[] cascadeTypes();

    boolean lazyInit() default true;
}
