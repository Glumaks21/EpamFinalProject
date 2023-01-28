package ua.maksym.hlushchenko.orm.entity.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {
    String mappedBy() default "";

    CascadeType[] cascadeTypes();

    boolean lazyInit() default false;
}
