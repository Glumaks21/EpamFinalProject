package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.orm.annotations.Column;
import ua.maksym.hlushchenko.orm.annotations.Table;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.*;

public class ObjectQueryParser {
    private static final String nameRegex = "((?!JOIN|WHERE|FROM|AND|ON|OR|DELETE|UPDATE|INSERT)\\b(\\w+)\\b)";

    private static final Pattern selectPattern = Pattern.compile(
            "FROM\\s+\\w+(\\s+WHERE\\s+\\w+)?",
            Pattern.CASE_INSENSITIVE);

    private final EntityManager entityManager;

    public ObjectQueryParser(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String parseQuery(String query) {
        if (selectPattern.matcher(query).matches()) {
            return parseSelectQuery(query);
        }

        return null;
    }

    private String parseSelectQuery(String query) {
        Matcher matcher = selectPattern.matcher(query);

        matcher.find();
        Class<?> clazz = entityManager.getEntity(matcher.group(7));

        String[] objectQueryFields = matcher.group(1).split("\\s*,\\s*");
        Field[] objectFields = clazz.getDeclaredFields();
        List<String> sqlFields = new ArrayList<>();

        String alias = matcher.group(9);
        for (String objectQueryField : objectQueryFields) {
            for (Field objectField : objectFields) {
                if (objectQueryField.equals(objectField.getName()) &&
                        objectField.isAnnotationPresent(Column.class)) {
                    Column annotation = objectField.getAnnotation(Column.class);
                    String sqlColumnName = annotation.value();
                    sqlFields.add(sqlColumnName);
                }
            }
        }

        Table annotation = clazz.getAnnotation(Table.class);
        String tableName = annotation.value();
        return null;
    }
}


