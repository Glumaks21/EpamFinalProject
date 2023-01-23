package ua.maksym.hlushchenko.dao.db.sql.entities.test5;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.annotations.Column;

@Getter
@Setter
public class C extends B {
    @Column("c_column")
    private int bColumn;
}
