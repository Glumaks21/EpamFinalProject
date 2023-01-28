package ua.maksym.hlushchenko.orm.entity.entitiesForTest.test4;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.entity.annotations.Column;

@Getter
@Setter
public class C extends B {
    @Column("c_column")
    private int bColumn;
}
