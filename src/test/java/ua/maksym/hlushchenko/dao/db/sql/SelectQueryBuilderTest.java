package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectQueryBuilderTest {

    @Test
    void addJoin() {
        SelectQueryBuilder builder = new SelectQueryBuilder().
                addMainTable("test");

        assertThrows(IllegalArgumentException.class,
                () -> builder.addJoin("failed", "failed", "a", "b"));
        assertDoesNotThrow(() -> builder.addJoin("test", "test1", "a", "b"));
        assertDoesNotThrow(() -> builder.addJoin("test1", "test2", "a", "b"));
    }

    @Test
    void testToString() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        assertThrows(IllegalStateException.class, builder::toString);

        builder.addMainTable("test");

        assertThrows(IllegalStateException.class, builder::toString);

        builder.addColumn("test", "first");

        assertEquals("SELECT test.first AS _test_first FROM test", builder.toString());

        builder.addAllColumns("test", List.of("second", "third"));

        assertEquals("SELECT test.first AS _test_first, " +
                "test.second AS _test_second, " +
                "test.third AS _test_third " +
                "FROM test", builder.toString());

        assertThrows(IllegalArgumentException.class,
                () -> builder.addJoin("join_table", "test", "column", "join_column"));
        assertDoesNotThrow( () -> builder.addJoin("test", "join_table", "column", "join_column"));

        assertEquals("SELECT test.first AS _test_first, " +
                "test.second AS _test_second, " +
                "test.third AS _test_third " +
                "FROM test " +
                "JOIN join_table ON test.column = join_table.join_column", builder.toString());

        builder.addColumn("join_table", "success");

        assertEquals("SELECT test.first AS _test_first, " +
                "test.second AS _test_second, " +
                "test.third AS _test_third, " +
                "join_table.success AS _join_table_success " +
                "FROM test " +
                "JOIN join_table ON test.column = join_table.join_column", builder.toString());
    }
}