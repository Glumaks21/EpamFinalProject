package ua.maksym.hlushchenko.orm.entity.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToOne {
    CascadeType[] cascadeTypes();

    boolean lazyInit() default false;
}
