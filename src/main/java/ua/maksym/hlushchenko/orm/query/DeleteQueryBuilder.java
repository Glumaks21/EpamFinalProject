package ua.maksym.hlushchenko.orm.query;

public final class DeleteQueryBuilder extends QueryBuilder<DeleteQueryBuilder>
        implements QueryBuilderWithConditions<DeleteQueryBuilder> {
    private final ConditionContainer conditionContainer = new ConditionContainer(getTableContainer());

    public DeleteQueryBuilder addCondition(String table, String column,
                                           ConditionOperator conditionOperator, Object value) {
        conditionContainer.addCondition(table, column, conditionOperator, value);
        return this;
    }

    public DeleteQueryBuilder addConditionConcatenation(ConditionOperator conditionOperator) {
        conditionContainer.addConditionConcatenation(conditionOperator);
        return this;
    }

    @Override
    public String toString() {
        return "DELETE FROM " +
                getTableContainer().getMainTable() +
                conditionContainer.getWhereString();
    }
}