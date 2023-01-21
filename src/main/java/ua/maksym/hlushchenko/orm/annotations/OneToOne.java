package ua.maksym.hlushchenko.orm.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {
    String mappedBy() default "";

    boolean lazyInit() default false;
}
