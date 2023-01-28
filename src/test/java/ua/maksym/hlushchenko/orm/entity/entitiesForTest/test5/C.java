package ua.maksym.hlushchenko.orm.entity.entitiesForTest.test5;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.entity.annotations.Column;
import ua.maksym.hlushchenko.orm.entity.annotations.Entity;

@Getter
@Setter
@Entity
public class C extends B {
    @Column("c_column")
    private int bColumn;
}
