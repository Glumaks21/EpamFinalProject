package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.exception.EntityParserException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityParserTest {
    @Test
    void getEntitiesHierarchyOf() {
        assertEquals(List.of(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test1.A.class),
                EntityParser.getEntityHierarchyOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test1.A.class));

        assertEquals(List.of(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test4.A.class,
                        ua.maksym.hlushchenko.dao.db.sql.entities.test4.B.class,
                        ua.maksym.hlushchenko.dao.db.sql.entities.test4.C.class),
                EntityParser.getEntityHierarchyOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test4.C.class));

        assertEquals(List.of(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test5.A.class,
                        ua.maksym.hlushchenko.dao.db.sql.entities.test5.B.class,
                        ua.maksym.hlushchenko.dao.db.sql.entities.test5.C.class),
                EntityParser.getEntityHierarchyOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test5.C.class));

        assertEquals(List.of(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test6.A.class,
                        ua.maksym.hlushchenko.dao.db.sql.entities.test6.B.class,
                        ua.maksym.hlushchenko.dao.db.sql.entities.test6.C.class),
                EntityParser.getEntityHierarchyOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test6.C.class));

        assertThrows(EntityParserException.class, () -> EntityParser.getEntityHierarchyOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test3.C.class));
    }

    @Test
    void getTableNameOf() {
        assertEquals("a", EntityParser.getTableNameOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test1.A.class));

        assertEquals("a", EntityParser.getTableNameOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test4.C.class));

        assertEquals("b", EntityParser.getTableNameOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test5.C.class));

        assertEquals("c", EntityParser.getTableNameOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test6.C.class));

        assertThrows(EntityParserException.class, () -> EntityParser.getTableNameOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test3.C.class));
    }

    @Test
    void getIdColumnNameOf() {
        assertEquals("a_id", EntityParser.getIdColumnNameOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test1.A.class));

        assertEquals("a_id", EntityParser.getIdColumnNameOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test4.C.class));

        assertEquals("a_id", EntityParser.getIdColumnNameOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test5.C.class));

        assertEquals("c_id", EntityParser.getIdColumnNameOf(
                        ua.maksym.hlushchenko.dao.db.sql.entities.test6.C.class));

        assertThrows(EntityParserException.class, () -> EntityParser.getIdColumnNameOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test3.C.class));
    }

    @Test
    void getColumnNameOf() throws NoSuchFieldException {
        Class<?> aClass = ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class;

        assertEquals("a_id", EntityParser.getColumnNameOf(
                aClass.getDeclaredField("aId")));

        assertEquals("a_column", EntityParser.getColumnNameOf(
                aClass.getDeclaredField("aColumn")));

        assertEquals("a_join_column", EntityParser.getColumnNameOf(
                aClass.getDeclaredField("aJoinColumn")));

        assertThrows(EntityParserException.class, () -> EntityParser.getColumnNameOf(
                aClass.getDeclaredField("aFake")));
    }

    @Test
    void getColumnNamesOf() {
        assertEquals(List.of("a_id", "a_column", "a_join_column"), EntityParser.getColumnNamesOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class));

        assertEquals(List.of("a_id", "a_column", "b_column", "c_column"), EntityParser.getColumnNamesOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test4.C.class));

        assertEquals(List.of("a_id", "b_column", "c_column"), EntityParser.getColumnNamesOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test5.C.class));

        assertEquals(List.of("c_id", "c_column"), EntityParser.getColumnNamesOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test6.C.class));

        assertThrows(EntityParserException.class, () -> EntityParser.getColumnNamesOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test3.C.class));
    }

    @Test
    void getIdValue() {
        getIdValueTest1();
        getIdValueTest4();
        getIdValueTest5();
        getIdValueTest6();
    }

    void getIdValueTest1() {
        ua.maksym.hlushchenko.dao.db.sql.entities.test2.A a =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test2.A();
        a.setAId(777);

        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class, a));
    }

    void getIdValueTest4() {
        ua.maksym.hlushchenko.dao.db.sql.entities.test4.C c =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test4.C();
        c.setAId(777);

        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test4.A.class, c));
        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test4.B.class, c));
        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test4.C.class, c));
    }

    void getIdValueTest5() {
        ua.maksym.hlushchenko.dao.db.sql.entities.test5.C c =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test5.C();
        c.setAId(777);

        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test5.A.class, c));
        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test5.B.class, c));
        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test5.C.class, c));
    }

    void getIdValueTest6() {
        ua.maksym.hlushchenko.dao.db.sql.entities.test6.C c =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test6.C();
        c.setAId(777);

        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test6.A.class, c));
        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test6.B.class, c));
        assertEquals(777, EntityParser.getIdValue(
                ua.maksym.hlushchenko.dao.db.sql.entities.test6.C.class, c));
    }

    @Test
    void getColumnValuesOf() {
        ua.maksym.hlushchenko.dao.db.sql.entities.test2.B b =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test2.B();
        b.setAId(777);
        b.setAColumn("Set");
        b.setAFake(404);
        ua.maksym.hlushchenko.dao.db.sql.entities.test2.C c =
                new  ua.maksym.hlushchenko.dao.db.sql.entities.test2.C();
        c.setCId(123);
        b.setAJoinColumn(c);

        assertEquals(List.of(777, "Set", 123), EntityParser.getColumnValuesOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class, b));
    }

    @Test
    void getValueOf() throws NoSuchFieldException {
        ua.maksym.hlushchenko.dao.db.sql.entities.test2.B b =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test2.B();
        b.setAId(777);
        b.setAColumn("Set");
        b.setAFake(404);
        ua.maksym.hlushchenko.dao.db.sql.entities.test2.C c =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test2.C();
        c.setCId(123);
        b.setAJoinColumn(c);

        assertEquals(777, EntityParser.getValueOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aId"), b));
        assertEquals("Set", EntityParser.getValueOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aColumn"), b));
        assertEquals(404.0, EntityParser.getValueOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aFake"), b));
        assertEquals(c, EntityParser.getValueOf(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aJoinColumn"), b));
    }

    @Test
    void setValueTo() throws NoSuchFieldException {
        ua.maksym.hlushchenko.dao.db.sql.entities.test2.B b =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test2.B();

        EntityParser.setValueTo(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aId"),
                777, b);
        EntityParser.setValueTo(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aColumn"),
                "Set", b);
        EntityParser.setValueTo(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aFake"),
                404, b);

        ua.maksym.hlushchenko.dao.db.sql.entities.test2.C c =
                new ua.maksym.hlushchenko.dao.db.sql.entities.test2.C();
        c.setCId(123);

        EntityParser.setValueTo(
                ua.maksym.hlushchenko.dao.db.sql.entities.test2.A.class.getDeclaredField("aJoinColumn"),
                c, b);

        assertEquals(777, b.getAId());
        assertEquals("Set", b.getAColumn());
        assertEquals(404.0, b.getAFake());
        assertEquals(c, b.getAJoinColumn());
    }
}