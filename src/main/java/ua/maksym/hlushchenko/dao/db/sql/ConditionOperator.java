package ua.maksym.hlushchenko.dao.db.sql;

enum ConditionOperator {
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
