package ua.maksym.hlushchenko.orm.dao;

import javassist.util.proxy.*;
import ua.maksym.hlushchenko.orm.entity.annotations.*;
import ua.maksym.hlushchenko.orm.exception.EntityParserException;
import ua.maksym.hlushchenko.orm.exception.MappingException;
import ua.maksym.hlushchenko.orm.query.QueryRelationUtil;
import ua.maksym.hlushchenko.orm.query.SelectQueryBuilder;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import static ua.maksym.hlushchenko.orm.entity.EntityParser.*;

public class GenericMapper<T> implements SqlMapper<T> {
    private final Class<? extends T> entityClass;
    private final Session session;

    public GenericMapper(Class<? extends T> entityClass, Session session) {
        this.entityClass = entityClass;
        this.session = session;
    }

    @Override
    public T map(ResultSet resultSet) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(entityClass);
            factory.setInterfaces(new Class[]{LoadProxy.class});
            Class<? extends T> proxyClass = (Class<? extends T>) factory.createClass();

            T proxyInstance = proxyClass.getConstructor().newInstance();
            setAllColumns(resultSet, entityClass, proxyInstance);

            ((ProxyObject) proxyInstance).setHandler(new ProxyHandler(session));
            return proxyInstance;
        } catch (InvocationTargetException | IllegalAccessException |
                 InstantiationException | NoSuchMethodException e) {
            throw new MappingException("Failed to generate proxy for " + entityClass, e);
        }
    }

    private void setAllColumns(ResultSet resultSet, Class<?> entityClass, Object instance) {
        try {
            List<Class<?>> hierarchy = getFullClassHierarchyOf(entityClass);
            for (Class<?> tableClass : hierarchy) {
                for (Field field : tableClass.getDeclaredFields()) {
                    mapField(field, resultSet, instance);
                }
            }
        } catch (SQLException | EntityParserException e) {
            throw new MappingException(e);
        }
    }

    private void mapField(Field field, ResultSet resultSet, Object instance) throws SQLException {
        Object value = null;
        if (field.isAnnotationPresent(Id.class) | field.isAnnotationPresent(Column.class)) {
            value = getMappedSingleColumnValue(field, resultSet);
        } else if (field.isAnnotationPresent(OneToOne.class) && !field.getAnnotation(OneToOne.class).lazyInit()) {
            value = getMappedOneToOneValue(field, resultSet);
        } else if (field.isAnnotationPresent(ManyToOne.class) && !field.getAnnotation(ManyToOne.class).lazyInit()) {
            value = getMappedManyToOneValue(field, resultSet);
        } else if (field.isAnnotationPresent(OneToMany.class) && !field.getAnnotation(OneToMany.class).lazyInit()) {
            value = getMappedOneToManyValue(field, resultSet);
        } else if (field.isAnnotationPresent(ManyToMany.class) && !field.getAnnotation(ManyToMany.class).lazyInit()) {
            value = getMappedManyToManyValue(field, resultSet);
        }

        if (value != null) {
            setValueTo(field, value, instance);
        }
    }

    private Object getMappedSingleColumnValue(Field field, ResultSet resultSet) throws SQLException {
        String tableName = getTableNameOf(field.getDeclaringClass());
        String columnName = getColumnNameOf(field);
        String resultSetColumn = SelectQueryBuilder.convertToResultSetColumn(tableName, columnName);
        return resultSet.getObject(resultSetColumn, field.getType());
    }

    private Object getMappedOneToOneValue(Field field, ResultSet resultSet) throws SQLException {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        try {
            Class<?> originEntity = field.getDeclaringClass();
            String sqlQuery;
            Object foreignKey;

            if (oneToOne.mappedBy().equals("")) {
                sqlQuery = QueryRelationUtil.getUnMappedOneToOneQueryFor(field);
                String resultSetForeignKeyColumn = SelectQueryBuilder.convertToResultSetColumn(
                        getTableNameOf(originEntity), getColumnNameOf(field));
                foreignKey = resultSet.getObject(resultSetForeignKeyColumn);
            } else {
                sqlQuery = QueryRelationUtil.getMappedOneToOneQueryFor(field);
                String resultSetForeignKeyColumn = SelectQueryBuilder.convertToResultSetColumn(
                        getTableNameOf(originEntity), getIdColumnNameOf(originEntity));
                foreignKey = resultSet.getObject(resultSetForeignKeyColumn);
            }

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), session);
            return dao.querySingle(sqlQuery, foreignKey).
                    orElseThrow(() -> new MappingException("Related entity wasn't found"));
        } catch (NoSuchFieldException e) {
            throw new MappingException("Field that mappedBy: " + oneToOne.mappedBy() +
                    " wasn't found", e);
        }
    }

    private Object getMappedManyToOneValue(Field field, ResultSet resultSet) throws SQLException {
        String sqlQuery = QueryRelationUtil.getQueryOfManyToOneFor(field);

        String resultSetForeignKeyColumn = SelectQueryBuilder.convertToResultSetColumn(
                getTableNameOf(field.getDeclaringClass()), getColumnNameOf(field));
        Object relatedIdValue = resultSet.getObject(resultSetForeignKeyColumn);

        AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), session);
        return dao.querySingle(sqlQuery, relatedIdValue).
                orElseThrow(() -> new MappingException("Related entity wasn't found"));
    }

    private List<?> getMappedOneToManyValue(Field field, ResultSet resultSet) throws SQLException {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        try {
            String sqlQuery = QueryRelationUtil.getQueryOfOneToManyFor(field);

            Class<?> entityClass = field.getDeclaringClass();
            String resultSetIdColumn = SelectQueryBuilder.convertToResultSetColumn(
                    getTableNameOf(entityClass), getIdColumnNameOf(entityClass));
            Object id = resultSet.getObject(resultSetIdColumn);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(oneToMany.genericType(), session);
            return dao.queryList(sqlQuery, id);
        } catch (NoSuchFieldException e) {
            throw new MappingException("Field that mappedBy: " + oneToMany.mappedBy() +
                    " wasn't found", e);
        }
    }

    private Object getMappedManyToManyValue(Field field, ResultSet resultSet) throws SQLException {
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        try {
            Class<?> originEntity = field.getDeclaringClass();
            Class<?> relatedEntity = manyToMany.genericType();

            String sqlQuery = QueryRelationUtil.getQueryOfManyToManyFor(field);

            String resultSetIdColumn = SelectQueryBuilder.convertToResultSetColumn(
                    getTableNameOf(originEntity), getIdColumnNameOf(originEntity));
            Object idValue = resultSet.getObject(resultSetIdColumn);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(relatedEntity, session);
            return dao.queryList(sqlQuery, idValue);
        } catch (NoSuchFieldException e) {
            throw new MappingException("Field that mappedBy: " + manyToMany.mappedBy() +
                    " wasn't found", e);
        }
    }
}
