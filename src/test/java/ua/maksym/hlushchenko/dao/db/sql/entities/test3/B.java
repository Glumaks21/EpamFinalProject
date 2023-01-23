package ua.maksym.hlushchenko.dao.db.sql.entities.test3;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.annotations.Column;

@Getter
@Setter
public class B extends A {
    @Column("b_column")
    private int bColumn;
}
