package ua.maksym.hlushchenko.dao.db.sql;

import java.sql.Statement;
import java.util.*;

class SqlQueryFormatter {
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
            } else if (nextWord.equalsIgnoreCase("JOIN")) {
                queryFormatBuilder.append("\n");
            } else {
                queryFormatBuilder.append(" ");
            }
        }

        queryFormatBuilder.append(words[words.length - 1]);
        return queryFormatBuilder.toString();
    }
}
