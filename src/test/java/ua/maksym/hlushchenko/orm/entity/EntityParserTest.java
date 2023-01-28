package ua.maksym.hlushchenko.orm.entity;

import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.orm.exception.EntityParserException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityParserTest {
    @Test
    void isEntityClass() {
        assertTrue(EntityParser.isEntity(ua.maksym.hlushchenko.orm.entity.entitiesForTest.test2.A.class));
        assertFalse(EntityParser.isEntity(ua.maksym.hlushchenko.orm.entity.entitiesForTest.test2.B.class));
    }

    @Test
    void isTableClass() {
        assertTrue(EntityParser.isEntity(ua.maksym.hlushchenko.orm.entity.entitiesForTest.test2.A.class));
        assertFalse(EntityParser.isEntity(ua.maksym.hlushchenko.orm.entity.entitiesForTest.test2.B.class));
    }

    @Test
    void isCascadeSaveField() {
    }

    @Test
    void isCascadeUpdateField() {
    }

    @Test
    void isCascadeDeleteField() {
    }

    @Test
    void getFullClassHierarchyOf() {
        assertThrows(EntityParserException.class,
                () -> EntityParser.getFullClassHierarchyOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test1.A.class));
        assertThrows(EntityParserException.class,
                () -> EntityParser.getFullClassHierarchyOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test2.B.class));
        assertThrows(EntityParserException.class,
                () -> EntityParser.getFullClassHierarchyOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test3.C.class));
        assertThrows(EntityParserException.class,
                () -> EntityParser.getFullClassHierarchyOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test4.C.class));

        assertEquals(List.of(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test5.A.class,
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test5.B.class,
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test5.C.class),
                EntityParser.getFullClassHierarchyOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test5.C.class));
        assertEquals(List.of(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.A.class,
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.B.class,
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.C.class,
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.D.class),
                EntityParser.getFullClassHierarchyOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.D.class));
    }

    @Test
    void getIdColumnNameOf() {
        assertEquals("a_id",
                EntityParser.getIdColumnNameOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test5.C.class));
        assertEquals("mapped_c_id",
                EntityParser.getIdColumnNameOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.D.class));
    }

    @Test
    void getColumnNameOf() throws NoSuchFieldException {
        Class<?> aClass = ua.maksym.hlushchenko.orm.entity.entitiesForTest.test7.A.class;

        assertEquals("a_id",
                EntityParser.getColumnNameOf(aClass.getDeclaredField("aId")));
        assertEquals("a_column",
                EntityParser.getColumnNameOf(aClass.getDeclaredField("aColumn")));
        assertEquals("a_join_column",
                EntityParser.getColumnNameOf(aClass.getDeclaredField("aJoinColumn")));
        assertThrows(EntityParserException.class,
                () -> EntityParser.getColumnNameOf(aClass.getDeclaredField("aFake")));
    }

    @Test
    void getColumnNamesOf() {
        assertEquals(List.of("a_id", "b_column", "c_column"),
                EntityParser.getColumnNamesOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test5.C.class));
        assertEquals(List.of("a_id", "a_column"),
                EntityParser.getColumnNamesOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.A.class));
        assertEquals(List.of("a_id", "a_column", "b_column"),
                EntityParser.getColumnNamesOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.B.class));
        assertEquals(List.of("mapped_c_id", "c_column"),
                EntityParser.getColumnNamesOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.C.class));
        assertEquals(List.of("mapped_c_id", "c_column", "d_column"),
                EntityParser.getColumnNamesOf(
                        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.D.class));
    }

    @Test
    void getIdValue() {
        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.B b6 =
                new   ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.B();

        b6.setAId(123);
        assertEquals(b6.getAId(), EntityParser.getIdValueOf(
                ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.A.class , b6));
        assertEquals(b6.getAId(), EntityParser.getIdValueOf(
                ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.B.class , b6));
        assertEquals(b6.getAId(), EntityParser.getIdValueOf(
                ua.maksym.hlushchenko.orm.entity.entitiesForTest.test6.C.class , b6));

        ua.maksym.hlushchenko.orm.entity.entitiesForTest.test8.B b8 =
                new ua.maksym.hlushchenko.orm.entity.entitiesForTest.test8.B();

        b8.setAId(123);
        assertEquals(b8.getAId(), EntityParser.getIdValueOf(
                ua.maksym.hlushchenko.orm.entity.entitiesForTest.test8.A.class , b8));
        assertEquals(b8.getAId(), EntityParser.getIdValueOf(
                ua.maksym.hlushchenko.orm.entity.entitiesForTest.test8.B.class , b8));
    }

    @Test
    void getColumnValuesOf() {
    }

    @Test
    void getValueOf() {
    }

    @Test
    void setValueTo() {
    }
}