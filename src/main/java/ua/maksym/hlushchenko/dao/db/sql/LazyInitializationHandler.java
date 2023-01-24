package ua.maksym.hlushchenko.dao.db.sql;

import javassist.util.proxy.MethodHandler;
import ua.maksym.hlushchenko.exception.LazyInitializationException;
import ua.maksym.hlushchenko.util.*;

import ua.maksym.hlushchenko.orm.annotations.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import static ua.maksym.hlushchenko.dao.db.sql.EntityParser.*;
import static ua.maksym.hlushchenko.dao.db.sql.QueryBuilder.*;


public class LazyInitializationHandler implements MethodHandler {
    private final Session session;
    private final Set<Method> invokedGetters = new HashSet<>();
    private boolean updated;

    public LazyInitializationHandler(Session session) {
        this.session = session;
    }

    @Override
    public Object invoke(Object self, Method overridden, Method forwarder, Object[] args) throws Throwable {
        String methodName = overridden.getName();
        if (methodName.startsWith("get") && !invokedGetters.contains(overridden)) {
            try {
                String fieldName = StringUtil.toLowerCapCase(overridden.getName().substring(3));

                Field field = null;
                List<Class<?>> hierarchy = getEntityHierarchyOf(self.getClass().getSuperclass());
                for (int i = hierarchy.size() - 1; i >= 0; i--) {
                    Class<?> ancestor = hierarchy.get(i);

                    if (ReflectionUtil.isContainsDeclaredField(ancestor, fieldName)) {
                        field = ancestor.getDeclaredField(fieldName);
                        break;
                    }
                }

                if (field == null) {
                    throw new NoSuchFieldException();
                }

                fetchValue(field, self);

                invokedGetters.add(overridden);
            } catch (NoSuchFieldException e) {
                throw new LazyInitializationException(e);
            }
        } else if (!updated && methodName.startsWith("set")) {
            updated = true;
        } else if (methodName.equals("isUpdated")) {
            return updated;
        }

        return forwarder.invoke(self, args);
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
                foreignKey = getIdValueFor(entityClass, proxy);
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
        Object id = getIdValueFor(entityClass, proxy);
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

    private Object getIdValueFor(Class<?> entityClass, Object proxy) {
        return ReflectionUtil.getDeclaredFieldsAnnotatedWith(entityClass, Id.class).stream().
                map(entityField -> getValueOf(entityField, proxy)).
                findFirst().orElseThrow(() -> new LazyInitializationException(
                        "Class " + entityClass + " doesn't contain " + Id.class));
    }

    private Object fetchManyToOneValue(Field field, Object proxy) {
        Class<?> entityClass = field.getDeclaringClass();
        Object id = getIdValueFor(entityClass, proxy);
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
            Object id = getIdValueFor(entityClass, proxy);

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
            Object idValue = getIdValueFor(entityClass, proxy);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(manyToMany.genericType(), session);
            return dao.queryList(sqlQuery, idValue);
        } catch (NoSuchFieldException e) {
            throw new LazyInitializationException("Field that mappedBy: " + manyToMany.mappedBy() +
                    " wasn't found", e);
        }
    }
}