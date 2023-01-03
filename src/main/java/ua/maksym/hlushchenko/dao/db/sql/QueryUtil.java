package ua.maksym.hlushchenko.dao.db.sql;

import java.util.List;

public class QueryUtil {
    public static String createSelect(String table, String... fields) {
        return String.format("SELECT %s FROM %s", String.join(", ", fields), table);
    }

    public static String createSelectWithConditions(
            String table, List<String> fields, List<String> conditions) {
        String withoutConditions = createSelect(table, fields.toArray(new String[0]));
        String joinedConditions = joinWithPlaceHolders(conditions.toArray(new String[0]));
        return String.format("%s WHERE %s", withoutConditions, joinedConditions);

    }

    private static String joinWithPlaceHolders(String... conditions) {
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < conditions.length; i++) {
            strb.append(conditions[i]);
            strb.append(" = ?");
            if (i != conditions.length - 1) {
                strb.append(", ");
            }
        }
        return strb.toString();
    }

    public static String createUpdate(String table, List<String> fields, List<String> conditions) {
        String joinedFieldsWithPlaceholders = joinWithPlaceHolders(fields.toArray(new String[0]));
        String joinedConditionsWithPlaceHolders = joinWithPlaceHolders(conditions.toArray(new String[0]));
        return String.format("UPDATE %s SET %s WHERE %s",
                table, joinedFieldsWithPlaceholders, joinedConditionsWithPlaceHolders);
    }

    public static String createInsert(String table, String... fields) {
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            strb.append("?");
            if (i != fields.length - 1) {
                strb.append(", ");
            }
        }
        return String.format("INSERT INTO %s(%s) VALUES(%s)",
                table, String.join(", ", fields), strb.toString());
    }

    public static String createDelete(String table, String... conditions) {
        return String.format("DELETE FROM %s WHERE %s", table, joinWithPlaceHolders(conditions));
    }
}
