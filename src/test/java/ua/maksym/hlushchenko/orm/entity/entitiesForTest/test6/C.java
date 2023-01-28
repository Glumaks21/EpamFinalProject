package ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.entity.annotations.Column;
import ua.maksym.hlushchenko.orm.entity.annotations.Entity;
import ua.maksym.hlushchenko.orm.entity.annotations.IdMappedByColumn;
import ua.maksym.hlushchenko.orm.entity.annotations.Table;

@Getter
@Setter
@Entity
@Table("c")
@IdMappedByColumn("mapped_c_id")
public class C extends B {
    @Column("c_column")
    private int cColumn;
}
