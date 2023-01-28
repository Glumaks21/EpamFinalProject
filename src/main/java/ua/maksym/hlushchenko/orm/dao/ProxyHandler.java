package ua.maksym.hlushchenko.orm.dao;

import javassist.util.proxy.MethodHandler;
import ua.maksym.hlushchenko.orm.query.QueryRelationUtil;
import ua.maksym.hlushchenko.orm.exception.LazyInitializationException;
import ua.maksym.hlushchenko.orm.entity.annotations.*;
import ua.maksym.hlushchenko.orm.query.SelectQueryBuilder;
import ua.maksym.hlushchenko.util.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import static ua.maksym.hlushchenko.orm.entity.EntityParser.*;


public class ProxyHandler implements MethodHandler {
    private final Session session;
    private final Set<Method> invokedGetters = new HashSet<>();
    private boolean changed;

    public ProxyHandler(Session session) {
        this.session = session;
    }

    @Override
    public Object invoke(Object self, Method overridden, Method forwarder, Object[] args) throws Throwable {
        String methodName = overridden.getName();
        if (methodName.startsWith("get") && !invokedGetters.contains(overridden)) {
            try {
                String fieldName = StringUtil.toLowerCapCase(overridden.getName().substring(3));

                Field field = getNearestDeclaredFieldByName(fieldName, self.getClass().getSuperclass());
                fetchValue(field, self);

                invokedGetters.add(overridden);
            } catch (NoSuchFieldException e) {
                throw new LazyInitializationException(e);
            }
        } else if (!changed && methodName.startsWith("set")) {
            changed = true;
        } else if (methodName.equals("isChanged")) {
            return changed;
        } else if (methodName.equals("setChanged")) {
            changed = (boolean) args[0];
            return null;
        }

        return forwarder.invoke(self, args);
    }

    private Field getNearestDeclaredFieldByName(String fieldName, Class<?> entityClass) throws NoSuchFieldException {
        List<Class<?>> tableClasses = getFullClassHierarchyOf(entityClass);
        for (int i = tableClasses.size() - 1; i >= 0; i--) {
            Class<?> tableClass = tableClasses.get(i);

            for (Field field : tableClass.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
        }

        if (isEntity(tableClasses.get(0).getSuperclass())) {
            return getNearestDeclaredFieldByName(fieldName, tableClasses.get(0).getSuperclass());
        }

        throw new NoSuchFieldException();
    }

    private void fetchValue(Field field, Object proxy) {
        Object value = null;
        if (field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).lazyInit()) {
            value = fetchOneToOneValue(field, proxy);
        } else if (field.isAnnotationPresent(ManyToOne.class) && field.getAnnotation(ManyToOne.class).lazyInit()) {
            value = fetchManyToOneValue(field, proxy);
        } else if (field.isAnnotationPresent(OneToMany.class) && field.getAnnotation(OneToMany.class).lazyInit()) {
            value = fetchOneToManyValue(field, proxy);
        } else if (field.isAnnotationPresent(ManyToMany.class) && field.getAnnotation(ManyToMany.class).lazyInit()) {
            value = fetchManyToManyValue(field, proxy);
        }

        if (value != null) {
            setValueTo(field, value, proxy);
        }
    }

    private Object fetchOneToOneValue(Field field, Object proxy) {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        try {
            Class<?> entityClass = field.getDeclaringClass();

            String sqlQuery;
            Object foreignKey;
            if (oneToOne.mappedBy().equals("")) {
                sqlQuery = QueryRelationUtil.getUnMappedOneToOneQueryFor(field);
                foreignKey = requestForeignKeyForMappedOneToOne(field, proxy);
            } else {
                sqlQuery = QueryRelationUtil.getMappedOneToOneQueryFor(field);
                foreignKey = getIdValueOf(entityClass, proxy);
            }

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), session);
            return dao.querySingle(sqlQuery, foreignKey).
                    orElseThrow(() -> new LazyInitializationException("Related entity wasn't found"));
        } catch (NoSuchFieldException e) {
            throw new LazyInitializationException("Field that mappedBy: " + oneToOne.mappedBy() +
                    " wasn't found", e);
        }
    }

    private Object requestForeignKeyForMappedOneToOne(Field field,  Object proxy) {
        Class<?> entityClass = field.getDeclaringClass();
        Object id = getIdValueOf(entityClass, proxy);
        String foreignKeyQuery = QueryRelationUtil.getSelectQueryForField(field);

        try (ResultSet resultSet = session.query(foreignKeyQuery, id)) {
            if (!resultSet.next()) {
                throw new LazyInitializationException(
                        "Entity " + entityClass + " with id " + id + " wasn't found in db");
            }
            String resultSetForeignKeyColumn = SelectQueryBuilder.convertToResultSetColumn(
                    getTableNameOf(entityClass), getColumnNameOf(field));
            return resultSet.getObject(resultSetForeignKeyColumn);
        } catch (SQLException e) {
            throw new LazyInitializationException(e);
        }
    }


    private Object fetchManyToOneValue(Field field, Object proxy) {
        Class<?> entityClass = field.getDeclaringClass();
        Object id = getIdValueOf(entityClass, proxy);
        String foreignKeyQuery = QueryRelationUtil.getSelectQueryForField(field);

        try (ResultSet resultSet = session.query(foreignKeyQuery, id)) {
            if (!resultSet.next()) {
                throw new LazyInitializationException(
                        "Entity " + entityClass + " with id " + id + " wasn't found in db");
            }

            String sqlQuery = QueryRelationUtil.getQueryOfManyToOneFor(field);

            String resultSetForeignKeyColumn = SelectQueryBuilder.convertToResultSetColumn(
                    getTableNameOf(entityClass), getColumnNameOf(field));
            Object relatedIdValue = resultSet.getObject(resultSetForeignKeyColumn);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), session);
            return dao.querySingle(sqlQuery, relatedIdValue).
                    orElseThrow(() -> new LazyInitializationException("Related entity wasn't found"));
        } catch (SQLException e) {
            throw new LazyInitializationException(e);
        }
    }

    private Object fetchOneToManyValue(Field field, Object proxy) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        try {
            String sqlQuery = QueryRelationUtil.getQueryOfOneToManyFor(field);

            Class<?> entityClass = field.getDeclaringClass();
            Object id = getIdValueOf(entityClass, proxy);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(oneToMany.genericType(), session);
            return dao.queryList(sqlQuery, id);
        } catch (NoSuchFieldException e) {
            throw new LazyInitializationException("Field that mappedBy: " + oneToMany.mappedBy() +
                    " wasn't found", e);
        }
    }

    private Object fetchManyToManyValue(Field field, Object proxy) {
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        try {
            Class<?> entityClass = field.getDeclaringClass();

            String sqlQuery = QueryRelationUtil.getQueryOfManyToManyFor(field);
            Object idValue = getIdValueOf(entityClass, proxy);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(manyToMany.genericType(), session);
            return dao.queryList(sqlQuery, idValue);
        } catch (NoSuchFieldException e) {
            throw new LazyInitializationException("Field that mappedBy: " + manyToMany.mappedBy() +
                    " wasn't found", e);
        }
    }
}