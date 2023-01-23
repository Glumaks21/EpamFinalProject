package ua.maksym.hlushchenko.dao.db.sql.entities.test6;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.annotations.Column;
import ua.maksym.hlushchenko.orm.annotations.IdMappedByColumn;
import ua.maksym.hlushchenko.orm.annotations.Table;

@Getter
@Setter
@Table("c")
@IdMappedByColumn("c_id")
public class C extends B {
    @Column("c_column")
    private int bColumn;
}
