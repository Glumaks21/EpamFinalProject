package ua.maksym.hlushchenko.orm.entity.entitiesForTest.test1;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.entity.annotations.Column;
import ua.maksym.hlushchenko.orm.entity.annotations.Entity;
import ua.maksym.hlushchenko.orm.entity.annotations.Table;

@Getter
@Setter
@Entity
@Table("a")
public class A {
    private int aId;

    @Column("a_column")
    private String aColumn;

    private double aFake;
}
