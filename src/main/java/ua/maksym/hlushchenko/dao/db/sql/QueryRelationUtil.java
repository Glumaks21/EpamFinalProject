package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.orm.annotations.*;

import java.lang.reflect.Field;
import java.util.List;

import static ua.maksym.hlushchenko.dao.db.sql.EntityParser.*;
import static ua.maksym.hlushchenko.dao.db.sql.QueryBuilder.*;

public class QueryRelationUtil {
    public static QueryBuilder generateSelectQueryBuilder(Class<?> entityClass) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryType.SELECT).
                setTable(getTableNameOf(entityClass)).
                addAllColumns(getTableNameOf(entityClass), getColumnNamesOf(entityClass));

        List<Class<?>> superTypes = getEntityHierarchyOf(entityClass);
        for (int i = 1; i < superTypes.size(); i++) {
            Class<?> prevType = superTypes.get(i - 1);
            Class<?> currType = superTypes.get(i);
            queryBuilder.addJoin(getTableNameOf(prevType), getTableNameOf(currType),
                            getIdColumnNameOf(prevType), getIdColumnNameOf(currType)).
                    addAllColumns(getTableNameOf(currType), getColumnNamesOf(currType));
        }

        return queryBuilder;
    }

    public static String getSelectQueryForField(Field field) {
        Class<?> entityClass = field.getDeclaringClass();
        return new QueryBuilder(QueryType.SELECT).
                setTable(getTableNameOf(entityClass)).
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
                addJoin(getTableNameOf(relatedEntity), params[0],
                        getIdColumnNameOf(relatedEntity), params[2]).
                addJoin(params[0], getTableNameOf(originEntity),
                        params[1], getIdColumnNameOf(originEntity)).
                addCondition(getTableNameOf(originEntity), getIdColumnNameOf(originEntity),
                        ConditionOperator.EQUALS).
                toString();
    }
}
