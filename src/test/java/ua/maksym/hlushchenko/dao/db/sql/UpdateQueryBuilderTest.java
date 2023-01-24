package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpdateQueryBuilderTest {

    @Test
    void testToString() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();

        assertThrows(IllegalStateException.class, builder::toString);

        builder.addMainTable("test");

        assertThrows(IllegalStateException.class, builder::toString);

        builder.addColumn("test", "first");

        assertEquals("UPDATE test SET first = ?", builder.toString());

        builder.addAllColumns("test", List.of("second", "third"));

        assertEquals("UPDATE test SET first = ?, second = ?, third = ?", builder.toString());

        builder.addAllValues(List.of(1, "two"));

        assertEquals("UPDATE test SET first = 1, second = 'two', third = ?", builder.toString());
    }
}