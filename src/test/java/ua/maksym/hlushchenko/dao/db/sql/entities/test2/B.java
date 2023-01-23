package ua.maksym.hlushchenko.dao.db.sql.entities.test2;

import ua.maksym.hlushchenko.orm.annotations.Column;

public class B extends A {
    @Column("bColumn")
    private String aColumn;
}
