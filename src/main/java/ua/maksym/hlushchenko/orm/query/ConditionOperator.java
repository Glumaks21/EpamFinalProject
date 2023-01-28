package ua.maksym.hlushchenko.orm.query;

public enum ConditionOperator {
    EQUALS("="), LOWER("<"), GREATER(">"), AND("AND"), OR("OR");

    final String value;

    ConditionOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
