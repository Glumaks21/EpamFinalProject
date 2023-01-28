package ua.maksym.hlushchenko.orm.entity.entitiesForTest.test4;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.entity.annotations.Column;

@Getter
@Setter
public class B extends A {
    @Column("b_column")
    private int bColumn;
}
