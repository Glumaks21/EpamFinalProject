package ua.maksym.hlushchenko.orm.entity.entitiesForTest.test3;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.entity.annotations.Column;
import ua.maksym.hlushchenko.orm.entity.annotations.Table;

@Getter
@Setter
@Table("b")
public class B extends A {
    @Column("b_column")
    private int bColumn;
}
