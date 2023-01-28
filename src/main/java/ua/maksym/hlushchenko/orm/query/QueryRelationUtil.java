package ua.maksym.hlushchenko.orm.query;

import ua.maksym.hlushchenko.orm.entity.annotations.JoinTable;
import ua.maksym.hlushchenko.orm.entity.annotations.ManyToMany;
import ua.maksym.hlushchenko.orm.entity.annotations.OneToMany;
import ua.maksym.hlushchenko.orm.entity.annotations.OneToOne;

import java.lang.reflect.Field;
import java.util.List;

import static ua.maksym.hlushchenko.orm.entity.EntityParser.*;

public class QueryRelationUtil {
    public static SelectQueryBuilder generateSelectQueryBuilder(Class<?> aClass) {
        List<Class<?>> hierarchy = getFullClassHierarchyOf(aClass);
        Class<?> mainTableClass = hierarchy.get(0);

        String prevTableName = getTableNameOf(mainTableClass);
        String prevIdColumnName = getIdColumnNameOf(mainTableClass);

        SelectQueryBuilder queryBuilder = new SelectQueryBuilder().
                addMainTable(prevTableName).
                addAllColumns(prevTableName, getColumnNamesOf(mainTableClass));

        for (int i = 1; i < hierarchy.size(); i++) {
            Class<?> currClass = hierarchy.get(i);

            if (isTableClass(currClass)) {
                String currTableName = getTableNameOf(currClass);
                String currIdColumnName = getIdColumnNameOf(currClass);

                queryBuilder.addJoin(prevTableName, currTableName, prevIdColumnName, currIdColumnName).
                        addAllColumns(currTableName, getColumnNamesOf(currClass));

                prevTableName = currTableName;
                prevIdColumnName = currIdColumnName;
            }
        }

        return queryBuilder;
    }

    public static String getSelectQueryForField(Field field) {
        Class<?> entityClass = field.getDeclaringClass();
        return new SelectQueryBuilder().
                addMainTable(getTableNameOf(entityClass)).
                addColumn(getTableNameOf(entityClass), getColumnNameOf(field)).
                addCondition(getTableNameOf(entityClass), getIdColumnNameOf(entityClass), ConditionOperator.EQUALS).
                toString();
    }

    public static String getUnMappedOneToOneQueryFor(Field field) {
        Class<?> relatedEntity = field.getType();
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableNameOf(relatedEntity), getIdColumnNameOf(relatedEntity), ConditionOperator.EQUALS).
                toString();
    }

    public static String getMappedOneToOneQueryFor(Field field) throws NoSuchFieldException {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        Class<?> relatedEntity = field.getType();
        Field mappedField = relatedEntity.getDeclaredField(oneToOne.mappedBy());
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableNameOf(relatedEntity), getColumnNameOf(mappedField), ConditionOperator.EQUALS).
                toString();
    }

    public static String getQueryOfManyToOneFor(Field field) {
        Class<?> relatedEntity = field.getType();
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableNameOf(relatedEntity), getIdColumnNameOf(relatedEntity), ConditionOperator.EQUALS).
                toString();
    }

    public static String getQueryOfOneToManyFor(Field field) throws NoSuchFieldException {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        Class<?> relatedEntity = oneToMany.genericType();
        Field mappedField = relatedEntity.getDeclaredField(oneToMany.mappedBy());
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableNameOf(relatedEntity),
                        getColumnNameOf(mappedField),
                        ConditionOperator.EQUALS).
                toString();
    }

    public static String getQueryOfManyToManyFor(Field field)
            throws NoSuchFieldException {
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        Class<?> originEntity = field.getDeclaringClass();
        Class<?> relatedEntity = manyToMany.genericType();
        String[] params = getJoinTableValuesFor(field);

        return generateSelectQueryBuilder(relatedEntity).
                addJoin(getTableNameOf(relatedEntity), params[0],
                        getIdColumnNameOf(relatedEntity), params[2]).
                addJoin(params[0], getTableNameOf(originEntity),
                        params[1], getIdColumnNameOf(originEntity)).
                addCondition(getTableNameOf(originEntity), getIdColumnNameOf(originEntity),
                        ConditionOperator.EQUALS).
                toString();
    }

    public static String[] getJoinTableValuesFor(Field field) {
        try {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (!manyToMany.mappedBy().equals("")) {
                Field mappedField = manyToMany.genericType().getDeclaredField(manyToMany.mappedBy());
                JoinTable joinTable = mappedField.getAnnotation(JoinTable.class);
                return new String[]{joinTable.name(), joinTable.inverseColumn(), joinTable.joinColumn()};
            }
            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            return new String[]{joinTable.name(), joinTable.joinColumn(), joinTable.inverseColumn()};
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
