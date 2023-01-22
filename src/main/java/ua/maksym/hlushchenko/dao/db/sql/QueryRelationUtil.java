package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.orm.annotations.*;

import java.lang.reflect.Field;
import java.util.List;

import static ua.maksym.hlushchenko.dao.db.sql.EntityUtil.*;
import static ua.maksym.hlushchenko.dao.db.sql.QueryBuilder.*;

public class QueryRelationUtil {
    public static QueryBuilder generateSelectQueryBuilder(Class<?> entityClass) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryType.SELECT).
                setTable(getTableName(entityClass)).
                addAllColumns(getTableName(entityClass), getColumnNames(entityClass));

        List<Class<?>> superTypes = getEntitiesHierarchyOf(entityClass);
        for (int i = 1; i < superTypes.size(); i++) {
            Class<?> prevType = superTypes.get(i - 1);
            Class<?> currType = superTypes.get(i);
            queryBuilder.addJoin(getTableName(prevType), getTableName(currType),
                            getIdColumnName(prevType), getIdColumnName(currType)).
                    addAllColumns(getTableName(currType), getColumnNames(currType));
        }

        return queryBuilder;
    }

    public static String getSelectQueryForField(Field field) {
        Class<?> entityClass = field.getDeclaringClass();
        return new QueryBuilder(QueryType.SELECT).
                setTable(getTableName(entityClass)).
                addColumn(getTableName(entityClass), getColumnNameFor(field)).
                addCondition(getTableName(entityClass), getIdColumnName(entityClass), ConditionOperator.EQUALS).
                toString();
    }

    public static String getUnMappedOneToOneQueryFor(Field field) {
        Class<?> relatedEntity = field.getType();
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableName(relatedEntity), getIdColumnName(relatedEntity), ConditionOperator.EQUALS).
                toString();
    }

    public static String getMappedOneToOneQueryFor(Field field) throws NoSuchFieldException {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        Class<?> relatedEntity = field.getType();
        Field mappedField = relatedEntity.getDeclaredField(oneToOne.mappedBy());
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableName(relatedEntity), getColumnNameFor(mappedField), ConditionOperator.EQUALS).
                toString();
    }

    public static String getQueryOfManyToOneFor(Field field) {
        Class<?> relatedEntity = field.getType();
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableName(relatedEntity), getIdColumnName(relatedEntity), ConditionOperator.EQUALS).
                toString();
    }

    public static String getQueryOfOneToManyFor(Field field) throws NoSuchFieldException {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        Class<?> relatedEntity = oneToMany.genericType();
        Field mappedField = relatedEntity.getDeclaredField(oneToMany.mappedBy());
        return generateSelectQueryBuilder(relatedEntity).
                addCondition(getTableName(relatedEntity),
                        getColumnNameFor(mappedField),
                        ConditionOperator.EQUALS).
                toString();
    }

    public static String getQueryOfManyToManyFor(Field field)
            throws NoSuchFieldException {
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        Class<?> originEntity = field.getDeclaringClass();
        Class<?> relatedEntity = manyToMany.genericType();

        String[] params;
        if (!manyToMany.mappedBy().equals("")) {
            Field mappedField = relatedEntity.getDeclaredField(manyToMany.mappedBy());
            JoinTable joinTable = mappedField.getAnnotation(JoinTable.class);
            params = new String[]{joinTable.name(), joinTable.inverseColumn(), joinTable.joinColumn()};
        } else {
            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            params = new String[]{joinTable.name(), joinTable.joinColumn(), joinTable.inverseColumn()};
        }

        return generateSelectQueryBuilder(relatedEntity).
                addJoin(getTableName(relatedEntity), params[0],
                        getIdColumnName(relatedEntity), params[2]).
                addJoin(params[0], getTableName(originEntity),
                        params[1], getIdColumnName(originEntity)).
                addCondition(getTableName(originEntity), getIdColumnName(originEntity),
                        ConditionOperator.EQUALS).
                toString();
    }
}
