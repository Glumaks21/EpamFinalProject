package ua.maksym.hlushchenko.orm.query;

import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.orm.query.ConditionOperator;
import ua.maksym.hlushchenko.orm.query.DeleteQueryBuilder;

import static org.junit.jupiter.api.Assertions.*;

class DeleteQueryBuilderTest {
    @Test
    void testToString() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();

        assertThrows(IllegalStateException.class, builder::toString);

        builder.addMainTable("test");

        assertEquals("DELETE FROM test", builder.toString());

        builder.addCondition("test", "first", ConditionOperator.EQUALS).
                addConditionConcatenation(ConditionOperator.OR).
                addCondition("test", "second", ConditionOperator.GREATER, 1).
                addConditionConcatenation(ConditionOperator.AND).
                addCondition("test", "third", ConditionOperator.LOWER, 4);

        assertEquals("DELETE FROM test " +
                "WHERE test.first = ? OR test.second > 1 AND test.third < 4", builder.toString());

        builder.addConditionConcatenation(ConditionOperator.EQUALS);

        assertThrows(IllegalStateException.class, builder::toString);
    }
}