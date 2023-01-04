package ua.maksym.hlushchenko.dao.db.sql;

import java.sql.Statement;
import java.util.*;

class QueryUtil {
    public static String createSelect(String table, List<String> fields) {
        StringBuilder queryBuilder = new StringBuilder("SELECT ");
        appendFields(queryBuilder, table, fields);
        queryBuilder.append(" FROM ").
                append(table);
        return queryBuilder.toString();
    }

    private static void appendFields(StringBuilder stringBuilder,
                                     String table, List<String> fields) {
        for (int i = 0; i < fields.size(); i++) {
            stringBuilder.
                    append(table).append(".").append(fields.get(i)).
                    append(" as ").
                    append("_").append(table).append("_").append(fields.get(i));
            if (i != fields.size() - 1) {
                stringBuilder.append(", ");
            }
        }
    }

    public static String createSelectWithConditions(
            String table, List<String> fields, List<String> conditions) {
        String simpleSelect = createSelect(table, fields);
        StringBuilder queryBuilder = new StringBuilder(simpleSelect).
                append(" WHERE ");
        appendFieldsWithPlaceholders(queryBuilder, table, conditions);
        return queryBuilder.toString();
    }

    private static void appendFieldsWithPlaceholders(StringBuilder stringBuilder,
                                                     String table, List<String> fields) {
        for (int i = 0; i < fields.size(); i++) {
            stringBuilder.
                    append(table).append(".").append(fields.get(i)).
                    append(" = ?");
            if (i != fields.size() - 1) {
                stringBuilder.append(", ");
            }
        }
    }

    public static String createSelectWithJoin(String targetTable,
                                              List<String> targetFields,
                                              String joinedTable,
                                              List<String> joinedFields,
                                              String targetColumn,
                                              String joinColumn) {
        StringBuilder queryBuilder = new StringBuilder("SELECT ");
        appendFields(queryBuilder, targetTable, targetFields);
        if (!targetFields.isEmpty() && !joinedFields.isEmpty()) {
            queryBuilder.append(", ");
        }
        appendFields(queryBuilder, joinedTable, joinedFields);
        queryBuilder.append(" FROM ").
                append(targetTable).
                append(" JOIN ").
                append(joinedTable).
                append(" ON ").
                append(targetTable).append(".").append(targetColumn).
                append(" = ").
                append(joinedTable).append(".").append(joinColumn);
        return queryBuilder.toString();
    }

    public static String createSelectWithJoinAndConditions(String targetTable,
                                              List<String> targetFields,
                                              String joinedTable,
                                              List<String> joinedFields,
                                              String targetColumn,
                                              String joinColumn,
                                               String conditionTargetTable,
                                               List<String> conditions) {
        String simpleJoin = createSelectWithJoin(targetTable, targetFields,
                joinedTable, joinedFields, targetColumn, joinColumn);
        StringBuilder queryBuilder = new StringBuilder(simpleJoin).
                append(" WHERE ");
        appendFieldsWithPlaceholders(queryBuilder, conditionTargetTable, conditions);
        return queryBuilder.toString();
    }

    public static String createUpdate(String table, List<String> fields, List<String> conditions) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE ").
                append(table).
                append(" SET ");
        appendFieldsWithPlaceholders(queryBuilder, table, fields);
        queryBuilder.append(" WHERE ");
        appendFieldsWithPlaceholders(queryBuilder, table, conditions);
        return queryBuilder.toString();
    }

    public static String createInsert(String table, List<String> fields) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ").
                append(table).
                append("(");
        for (int i = 0; i < fields.size(); i++) {
            queryBuilder.append(fields.get(i));
            if (i != fields.size() - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(") VALUES(");
        for (int i = 0; i < fields.size(); i++) {
            queryBuilder.append("?");
            if (i != fields.size() - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(")");
        return queryBuilder.toString();
    }

    public static String createDelete(String table, List<String> conditions) {
        StringBuilder queryBuilder = new StringBuilder("DELETE FROM ").
                append(table).
                append(" WHERE ");
        appendFieldsWithPlaceholders(queryBuilder, table, conditions);
        return queryBuilder.toString();
    }

    public static String formatSql(Statement statement) {
        Objects.requireNonNull(statement);
        String dirtySqlQuery = statement.toString();
        return formatSql(dirtySqlQuery.substring(dirtySqlQuery.indexOf(": ") + 1));
    }

    public static String formatSql(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty query: " + sqlQuery);
        }

        StringBuilder queryFormatBuilder = new StringBuilder();
        String[] words = sqlQuery.trim().split("\\s+");
        boolean isListing = false;

        for (int i = 0; i < words.length - 1; i++) {
            String nextWord = words[i + 1];
            String word = words[i];

            queryFormatBuilder.append(word);
            if (word.equalsIgnoreCase("SELECT") ||
                word.equalsIgnoreCase("FROM") ||
                word.equalsIgnoreCase("WHERE") ||
                word.equalsIgnoreCase("SET")) {
                queryFormatBuilder.append("\n\t");
                isListing = true;
            } else if (isListing && word.endsWith(",")) {
                queryFormatBuilder.append("\n\t");
            } else if (isListing && (nextWord.equalsIgnoreCase("SELECT") ||
                    nextWord.equalsIgnoreCase("FROM") ||
                    nextWord.equalsIgnoreCase("WHERE") ||
                    nextWord.equalsIgnoreCase("SET"))) {
                isListing = false;
                queryFormatBuilder.append("\n");
            } else {
                queryFormatBuilder.append(" ");
            }
        }

        queryFormatBuilder.append(words[words.length - 1]);
        return queryFormatBuilder.toString();
    }
}
