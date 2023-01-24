package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ua.maksym.hlushchenko.dao.db.sql.QueryBuilder.*;

class QueryBuilderTest {
    @Test
    void tableContainerAddMainTable() {
        TableContainer tableContainer = new TableContainer();

        assertDoesNotThrow(() -> tableContainer.addMainTable("test"));
        assertThrows(IllegalStateException.class, () -> tableContainer.addMainTable("test2"));
    }

    @Test
    void columnContainerAdd() {
        TableContainer tableContainer = new TableContainer();
        ColumnContainer columnContainer = new ColumnContainer(tableContainer);

        assertThrows(IllegalStateException.class, () -> columnContainer.add("test", "failed"));

        tableContainer.addMainTable("test");

        assertDoesNotThrow(() -> columnContainer.add("test", "first"));
        assertDoesNotThrow(() -> columnContainer.add("test", "second"));
        assertThrows(IllegalArgumentException.class, () -> columnContainer.add("failed", "third"));
    }

    @Test
    void valueContainerAdd() {
        TableContainer tableContainer = new TableContainer();
        ColumnContainer columnContainer = new ColumnContainer(tableContainer);
        ValueContainer valueContainer = new ValueContainer(columnContainer);
        tableContainer.addMainTable("test");

        assertThrows(IllegalStateException.class, () -> valueContainer.add(1));

        columnContainer.add("test", "first");

        assertDoesNotThrow(() -> valueContainer.add(1));
        assertThrows(IllegalStateException.class, () -> valueContainer.add(2));

        columnContainer.add("test", "seconds");

        assertDoesNotThrow(() -> valueContainer.add("two"));
    }

    @Test
    void addCondition() {
        TableContainer tableContainer = new TableContainer();
        ConditionContainer conditionContainer = new ConditionContainer(tableContainer);
        tableContainer.addMainTable("test");

        assertThrows(IllegalArgumentException.class,
                () -> conditionContainer.addCondition("failed", "second",
                        ConditionOperator.EQUALS, null));
        assertDoesNotThrow(() ->
                conditionContainer.addCondition("test", "first",
                        ConditionOperator.EQUALS, "Yeeeep"));
        assertThrows(IllegalStateException.class,
                () -> conditionContainer.addCondition("test", "fourth",
                        ConditionOperator.EQUALS, "?"));

        conditionContainer.addConditionConcatenation(ConditionOperator.OR);

        assertDoesNotThrow(() ->
                conditionContainer.addCondition("test", "second", ConditionOperator.EQUALS, null));
    }

    @Test
    void addConditionConcatenation() {
        TableContainer tableContainer = new TableContainer();
        ConditionContainer conditionContainer = new ConditionContainer(tableContainer);
        tableContainer.addMainTable("test");

        assertThrows(IllegalStateException.class,
                () -> conditionContainer.addConditionConcatenation(ConditionOperator.OR));

        conditionContainer.addCondition("test", "column", ConditionOperator.EQUALS, null);

        assertDoesNotThrow(() -> conditionContainer.addConditionConcatenation(ConditionOperator.AND));
        assertThrows(IllegalStateException.class,
                () -> conditionContainer.addConditionConcatenation(ConditionOperator.AND));
    }
}