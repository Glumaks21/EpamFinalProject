package ua.maksym.hlushchenko.dao.db.sql;

public interface QueryBuilderWithConditions<T extends QueryBuilderWithConditions<?>> {
    T addCondition(String table, String column,
                   ConditionOperator conditionOperator, Object value);

    default T addCondition(String table, String column,
                           ConditionOperator conditionOperator) {
        return addCondition(table, column, conditionOperator, null);
    }

    T addConditionConcatenation(ConditionOperator conditionOperator);
}
