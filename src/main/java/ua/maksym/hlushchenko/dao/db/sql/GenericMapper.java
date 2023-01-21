package ua.maksym.hlushchenko.dao.db.sql;

import javassist.util.proxy.*;
import ua.maksym.hlushchenko.exception.*;
import ua.maksym.hlushchenko.orm.annotations.*;
import ua.maksym.hlushchenko.util.ReflectionUtil;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import static ua.maksym.hlushchenko.dao.db.sql.EntityUtil.*;
import static ua.maksym.hlushchenko.dao.db.sql.QueryBuilder.*;

public class GenericMapper<T> implements SqlMapper<T> {
    private final Class<T> entityClass;
    private final Connection connection;

    public GenericMapper(Class<T> entityClass, Connection connection) {
        this.entityClass = entityClass;
        this.connection = connection;
    }

    @Override
    public T map(ResultSet resultSet) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(entityClass);
            Class<? extends T> proxyClass = (Class<? extends T>) factory.createClass();

            T proxyInstance = proxyClass.getConstructor().newInstance();
            setAllColumns(resultSet, proxyInstance);

            ((ProxyObject) proxyInstance).setHandler(new LazyInitializationHandler());
            return proxyInstance;
        } catch (InvocationTargetException | IllegalAccessException |
                 InstantiationException | NoSuchMethodException e) {
            throw new MappingException("Failed to generate proxy for " + entityClass, e);
        }
    }

    private void setAllColumns(ResultSet resultSet, Object instance) {
        try {
            List<Class<?>> hierarchy = getEntitiesHierarchyOf(entityClass);
            for (int i = hierarchy.size() - 1; i >= 0; i--) {
                for (Field field : hierarchy.get(i).getDeclaredFields()) {
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
        String tableName = getTableName(field.getDeclaringClass());
        String columnName = getColumnNameFor(field);
        String resultSetColumn = convertToResultSetColumn(tableName, columnName);
        return resultSet.getObject(resultSetColumn, field.getType());
    }

    private Object getMappedOneToOneValue(Field field, ResultSet resultSet) throws SQLException {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        try {
            String sqlQuery = QueryRelationUtil.getQueryOfOneToOneFor(field);

            Class<?> originEntity = field.getDeclaringClass();
            String resultSetForeignKeyColumn = oneToOne.mappedBy().equals("") ?
                    convertToResultSetColumn(getTableName(originEntity), getColumnNameFor(field)) :
                    convertToResultSetColumn(getTableName(originEntity), getIdColumnName(originEntity));
            Object foreignKey = resultSet.getObject(resultSetForeignKeyColumn);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), connection);
            return dao.querySingle(sqlQuery, foreignKey).
                    orElseThrow(() -> new MappingException("Related entity wasn't found"));
        } catch (NoSuchFieldException e) {
            throw new MappingException("Field that mappedBy: " + oneToOne.mappedBy() +
                    " wasn't found", e);
        }
    }

    private Object getMappedManyToOneValue(Field field, ResultSet resultSet) throws SQLException {
        String sqlQuery = QueryRelationUtil.getQueryOfManyToOneFor(field);

        String resultSetForeignKeyColumn = convertToResultSetColumn(
                getTableName(field.getDeclaringClass()), getColumnNameFor(field));
        Object relatedIdValue = resultSet.getObject(resultSetForeignKeyColumn);

        AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), connection);
        return dao.querySingle(sqlQuery, relatedIdValue).
                orElseThrow(() -> new MappingException("Related entity wasn't found"));
    }

    private List<?> getMappedOneToManyValue(Field field, ResultSet resultSet) throws SQLException {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        try {
            String sqlQuery = QueryRelationUtil.getQueryOfOneToManyFor(field);

            Class<?> entityClass = field.getDeclaringClass();
            String resultSetIdColumn = convertToResultSetColumn(
                    getTableName(entityClass), getIdColumnName(entityClass));
            Object idValue = resultSet.getObject(resultSetIdColumn);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(oneToMany.genericType(), connection);
            return dao.queryList(sqlQuery, idValue);
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

            String resultSetIdColumn = convertToResultSetColumn(
                    getTableName(originEntity), getIdColumnName(originEntity));
            Object idValue = resultSet.getObject(resultSetIdColumn);

            AbstractSqlDao<Object, ?> dao = new GenericDao<>(relatedEntity, connection);
            return dao.queryList(sqlQuery, idValue);
        } catch (NoSuchFieldException e) {
            throw new MappingException("Field that mappedBy: " + manyToMany.mappedBy() +
                    " wasn't found", e);
        }
    }

    public class LazyInitializationHandler implements MethodHandler {
        private final Set<Method> invokedGetters = new HashSet<>();

        @Override
        public Object invoke(Object self, Method overridden, Method forwarder, Object[] args) throws Throwable {
            if (overridden.getName().startsWith("get") && !invokedGetters.contains(overridden)) {
                try {
                    String fieldName = StringUtil.toLowerCapCase(overridden.getName().substring(3));

                    Field field = null;
                    for (Class<?> curClass : getEntitiesHierarchyOf(self.getClass().getSuperclass())) {
                        if (ReflectionUtil.isContainsDeclaredField(curClass, fieldName)) {
                            field = curClass.getDeclaredField(fieldName);
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
                value = fetchOneToManyValue(field);
            } else if (field.isAnnotationPresent(ManyToMany.class) && field.getAnnotation(ManyToMany.class).lazyInit()) {
                value = fetchManyToManyValue(field);
            }

            if (value != null) {
                setValueTo(field, value, proxy);
            }
        }

        private Object fetchOneToOneValue(Field field, Object proxy) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            try {
                String sqlQuery = QueryRelationUtil.getQueryOfOneToOneFor(field);
                Object foreignKey = requestForeignKeyForOneToOne(field, proxy);
                AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), connection);
                return dao.querySingle(sqlQuery, foreignKey).
                        orElseThrow(() -> new MappingException("Related entity wasn't found"));
            } catch (NoSuchFieldException e) {
                throw new MappingException("Field that mappedBy: " + oneToOne.mappedBy() +
                        " wasn't found", e);
            }
        }

        private Object requestForeignKeyForOneToOne(Field field, Object proxy) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            Object id = ReflectionUtil.getDeclaredFieldsAnnotatedWith(entityClass,Id.class).stream().
                    map(entityField -> getValueOf(entityField, proxy)).
                    findFirst().orElseThrow(() -> new LazyInitializationException(
                            "Class " + entityClass + " doesn't contain " + Id.class));

            if (oneToOne.mappedBy().equals("")) {
                String foreignKeyQuery = new QueryBuilder(QueryType.SELECT).
                        setTable(getTableName(entityClass)).
                        addColumn(getTableName(entityClass), getColumnNameFor(field)).
                        addCondition(getTableName(entityClass), getIdColumnName(entityClass), ConditionOperator.EQUALS).
                        toString();

                SqlQueryHelper sqlQueryHelper = new SqlQueryHelper(connection);
                try (ResultSet resultSet = sqlQueryHelper.query(foreignKeyQuery, id)) {
                    if (!resultSet.next()) {
                        throw new LazyInitializationException(
                                "Entity " + entityClass + " with id " + id + " wasn't found in db");
                    }
                    String resultSetForeignKeyColumn = convertToResultSetColumn(
                            getTableName(entityClass), getColumnNameFor(field));
                    return resultSet.getObject(resultSetForeignKeyColumn);
                } catch (SQLException e) {
                    throw new LazyInitializationException(e);
                }
            }

            return id;
        }

        private Object fetchManyToOneValue(Field field, Object proxy) {
            Class<?> entityClass = field.getDeclaringClass();

            String foreignKeyQuery = new QueryBuilder(QueryType.SELECT).
                    setTable(getTableName(entityClass)).
                    addColumn(getTableName(entityClass), getColumnNameFor(field)).
                    addCondition(getTableName(entityClass), getIdColumnName(entityClass), ConditionOperator.EQUALS).
                    toString();

            Object id = ReflectionUtil.getDeclaredFieldsAnnotatedWith(entityClass,Id.class).stream().
                    map(entityField -> getValueOf(entityField, proxy)).
                    findFirst().orElseThrow(() -> new LazyInitializationException(
                            "Class " + entityClass + " doesn't contain " + Id.class));

            SqlQueryHelper sqlQueryHelper = new SqlQueryHelper(connection);
            try (ResultSet resultSet = sqlQueryHelper.query(foreignKeyQuery, id)) {
                if (!resultSet.next()) {
                    throw new LazyInitializationException(
                            "Entity " + entityClass + " with id " + id + " wasn't found in db");
                }

                String sqlQuery = QueryRelationUtil.getQueryOfManyToOneFor(field);

                String resultSetForeignKeyColumn = convertToResultSetColumn(getTableName(entityClass), getColumnNameFor(field));
                Object relatedIdValue = resultSet.getObject(resultSetForeignKeyColumn);

                AbstractSqlDao<Object, ?> dao = new GenericDao<>(field.getType(), connection);
                return dao.querySingle(sqlQuery, relatedIdValue).
                        orElseThrow(() -> new MappingException("Related entity wasn't found"));
            } catch (SQLException e) {
                throw new LazyInitializationException(e);
            }
        }

        private Object fetchOneToManyValue(Field field) {
            return null;
        }

        private Object fetchManyToManyValue(Field field) {
            return null;
        }
    }
}
