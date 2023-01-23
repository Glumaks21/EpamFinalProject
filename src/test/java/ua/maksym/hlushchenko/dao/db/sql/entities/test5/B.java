package ua.maksym.hlushchenko.dao.db.sql.entities.test5;

import lombok.Getter;
import lombok.Setter;
import ua.maksym.hlushchenko.orm.annotations.Column;
import ua.maksym.hlushchenko.orm.annotations.Table;

@Getter
@Setter
@Table("b")
public class B extends A {
    @Column("b_column")
    private int bColumn;
}
