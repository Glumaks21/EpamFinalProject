package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InsertQueryBuilderTest {
    @Test
    void testToString() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        assertThrows(IllegalStateException.class, builder::toString);

        builder.addMainTable("test");

        assertThrows(IllegalStateException.class, builder::toString);

        builder.addColumn("test", "first");

        assertEquals("INSERT INTO test(first) VALUES(?)", builder.toString());

        builder.addAllColumns("test", List.of("second", "third"));

        assertEquals("INSERT INTO test(first, second, third) VALUES(?, ?, ?)", builder.toString());

        builder.addAllValues(List.of(1, "two"));

        assertEquals("INSERT INTO test(first, second, third) VALUES(1, 'two', ?)", builder.toString());
    }
}