package ua.maksym.hlushchenko.orm.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumn {
    String value();

    boolean lazyInit() default false;
}
