package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.dao.db.sql.annotations.*;
import ua.maksym.hlushchenko.exception.*;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import static ua.maksym.hlushchenko.dao.db.sql.EntityParser.*;
import static ua.maksym.hlushchenko.util.ReflectionUtil.*;

public class GenericDao<K, T, C extends T> extends AbstractSqlDao<K, T> {
    private final String SQL_SELECT_ALL;
    private final String SQL_SELECT_BY_ID;
    private final String SQL_UPDATE_BY_ID;
    private final String SQL_INSERT;
    private final String SQL_DELETE_BY_ID;

    private final Class<C> entityClass;
    private final SqlMapper<C> mapper;

    private static final Logger log = LoggerFactory.getLogger(GenericDao.class);

    public GenericDao(Class<C> clazz, Connection connection) {
        super(connection);
        this.entityClass = clazz;

        mapper = new GenericMapper();

        SQL_SELECT_ALL = GenericQueryCreator.generateQueryByMethodName(clazz, "findAll");
        SQL_SELECT_BY_ID = GenericQueryCreator.generateQueryByMethodName(clazz, "find");
        SQL_INSERT = GenericQueryCreator.generateQueryByMethodName(clazz, "save");
        SQL_UPDATE_BY_ID = GenericQueryCreator.generateQueryByMethodName(clazz, "update");
        SQL_DELETE_BY_ID = GenericQueryCreator.generateQueryByMethodName(clazz, "delete");
    }

    @Override
    protected C mapEntity(ResultSet resultSet) {
        C entity = mapper.map(resultSet);
        return (C) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                entityClass.getInterfaces(),
                new LoadHandler(entity));
    }

    private class LoadHandler implements InvocationHandler {
        private final C wrapped;
        private final Set<Method> requestedGetters;

        public LoadHandler(C wrapped) {
            this.wrapped = wrapped;
            requestedGetters = new HashSet<>();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().startsWith("get") && !requestedGetters.contains(method)) {
                String fieldName = StringUtil.toLowerCapCase(method.getName().substring(3));
                Field field = wrapped.getClass().getDeclaredField(fieldName);

                Object value = null;
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    //value = fetchJoinedValue(field);
                    System.out.println(value);
                } else if (field.isAnnotationPresent(OneToMany.class)) {
                    //value = findOneToManyValues(field, resultSet);
                } else if (field.isAnnotationPresent(ManyToMany.class)) {
                    //value = findManyToManyValues(field, resultSet);
                }

                if (value != null) {
                    Method setter = getSetter(field);
                    setter.invoke(wrapped, value);
                }

                requestedGetters.add(method);
            }


            return method.invoke(wrapped, args);
        }

//        private Object fetchJoinedValue(Field field) {
//            Class<?> originEntity = field.getDeclaringClass();
//            Class<?> relatedEntity = extractJoinedClass(field);
//
//            String sqlQuery = new QueryBuilder(QueryBuilder.QueryType.SELECT).
//                    setTable(getTableName(originEntity)).
//                    addColumn(getTableName(originEntity), getColumnNameFor(field)).
//                    addCondition(getTableName(originEntity),
//                            getIdColumnName(originEntity),
//                            QueryBuilder.ConditionOperator.EQUALS).
//                    toString();
//            ResultSet resultSet = updateQueryWithKeys(sqlQuery, getIdValue(wrapped));
//
//            String sqlQuery = generateSelectQueryBuilder(joinedEntityClass).
//                    addCondition(getTableName(joinedEntityClass),
//                            getIdColumnName(joinedEntityClass),
//                            QueryBuilder.ConditionOperator.EQUALS).
//                    toString();
//            SqlMapper<?> mapper = new GenericMapper(joinedEntityClass, connection);
//            return mappedQuery(mapper, sqlQuery, foreignKey);
//        }
//
//        private QueryBuilder generateSelectQueryBuilder(Class<?> entityClass) {
//            QueryBuilder queryBuilder = new QueryBuilder(QueryBuilder.QueryType.SELECT).
//                    setTable(getTableName(entityClass)).
//                    addAllColumns(getTableName(entityClass), getColumnNames(entityClass));
//
//            List<Class<?>> superTypes = getEntitiesHierarchyOf(entityClass);
//            for (int i = 1; i < superTypes.size(); i++) {
//                Class<?> prevType = superTypes.get(i - 1);
//                Class<?> currType = superTypes.get(i);
//                queryBuilder.addJoin(getTableName(prevType), getTableName(currType),
//                                getIdColumnName(prevType), getIdColumnName(currType)).
//                        addAllColumns(getTableName(currType), getColumnNames(currType));
//            }
//
//            return queryBuilder;
//        }

        private Class<?> extractJoinedClass(Field field) {
            Class<?> joinedEntityClass;
            if (field.isAnnotationPresent(OneToOne.class)) {
                OneToOne oneToOne = field.getAnnotation(OneToOne.class);
                joinedEntityClass = oneToOne.relatedEntity();
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                joinedEntityClass = manyToOne.relatedEntity();
            } else {
                throw new MappingException("Relation between table is annotated incorrect");
            }
            return joinedEntityClass;
        }
    }

    @Override
    public List<T> findAll() {
        return queryList(SQL_SELECT_ALL);
    }

    @Override
    public Optional<T> find(K id) {
        return querySingle(SQL_SELECT_BY_ID, id);
    }

    @Override
    public void save(T entity) {
        try {
            int keyGenType = Statement.NO_GENERATED_KEYS;
            List<Object> args = getColumnValues(entityClass);

            Field idField = getDeclaredFieldsAnnotatedWith(entityClass, Id.class).get(0);
            if (idField.getAnnotation(Id.class).autoGenerated()) {
                keyGenType = Statement.RETURN_GENERATED_KEYS;
            }

            ResultSet resultSet = updateQueryWithKeys(SQL_INSERT, keyGenType, args.toArray(new Object[0]));
            if (resultSet.next()) {
                Method setter = getSetter(idField);
                Object generatedValue = resultSet.getObject(1, idField.getType());
                setter.invoke(entity, generatedValue);
            }
            resultSet.close();
        } catch (SQLException | IllegalAccessException | InvocationTargetException | EntityParserException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(T entity) {
        try {
            List<Object> args = getColumnValues(entityClass);

            Field idField = getDeclaredFieldsAnnotatedWith(entityClass, Id.class).get(0);
            Method getter = getGetter(idField);
            args.add(getter.invoke(entity));

            updateQuery(SQL_UPDATE_BY_ID, args.toArray(new Object[0]));
        } catch (IllegalAccessException | InvocationTargetException | EntityParserException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void delete(K id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }

    private QueryBuilder generateSelectQueryBuilder(Class<?> entityClass) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryBuilder.QueryType.SELECT).
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

    private class GenericMapper implements SqlMapper<C> {
        @Override
        public C map(ResultSet resultSet) {
            try {
                Object instance = entityClass.getConstructor().newInstance();

                List<Class<?>> superEntities = getEntitiesHierarchyOf(entityClass);
                Collections.reverse(superEntities);
                for (Class<?> entity : superEntities) {
                    setColumnFieldsForEntity(entity, resultSet, instance);
                }

                return (C) instance;
            } catch (InstantiationException | InvocationTargetException |
                     NoSuchMethodException | IllegalAccessException e) {
                throw new MappingException("Class is not POJO clas", e);
            } catch (SQLException e) {
                throw new MappingException("Error occurs while request an entity", e);
            }
        }

        private void setColumnFieldsForEntity(Class<?> entity, ResultSet resultSet, Object instance)
                throws SQLException, InvocationTargetException, IllegalAccessException {
            for (Field field : entity.getDeclaredFields()) {
                Object value = findValueForField(field, resultSet);

                if (value != null) {
                    Method setter = getSetter(field);
                    setter.invoke(instance, value);
                }
            }
        }

        private Object findValueForField(Field field, ResultSet resultSet) throws SQLException {
            Object value = null;
            if (field.isAnnotationPresent(Id.class) | field.isAnnotationPresent(Column.class)) {
                value = findSingleColumnValue(field, resultSet);
            } else if (field.isAnnotationPresent(OneToOne.class)) {
                value = findOneToOneValue(field, resultSet);
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                value = findManyToOneValue(field, resultSet);
            } else if (field.isAnnotationPresent(OneToMany.class)) {
                value = findOneToManyValues(field, resultSet);
            } else if (field.isAnnotationPresent(ManyToMany.class)) {
                value = findManyToManyValues(field, resultSet);
            }
            return value;
        }

        private Object findSingleColumnValue(Field field, ResultSet resultSet) throws SQLException {
            String columnName = extractColumnNameFromAnnotatedField(field);
            String resultSetColumn = QueryBuilder.convertToResultSetColumn(
                    getTableName(field.getDeclaringClass()), columnName);
            return resultSet.getObject(resultSetColumn, field.getType());
        }

        private Object findOneToOneValue(Field field, ResultSet resultSet) throws SQLException {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (oneToOne.lazyInit()) {
                return null;
            }

            Class<?> relatedEntity = oneToOne.relatedEntity();
            String sqlQuery = getSqlQueryForOneToOne(oneToOne, relatedEntity);

            Class<?> originEntity = field.getDeclaringClass();
            Object foreignKey = getForeignKeyForOneToOne(oneToOne, joinColumn, originEntity, resultSet);

            GenericDao<Object, Object, ?> dao = new GenericDao<>(relatedEntity, connection);
            return dao.querySingle(sqlQuery, foreignKey).
                    orElseThrow(() -> new MappingException("Related entity wasn't found"));
        }

        private String getSqlQueryForOneToOne(OneToOne oneToOne, Class<?> relatedEntity) {
            if (oneToOne.mappedBy().equals("")) {
                return generateSelectQueryBuilder(relatedEntity).
                        addCondition(getTableName(relatedEntity),
                                getIdColumnName(relatedEntity),
                                QueryBuilder.ConditionOperator.EQUALS).
                        toString();
            }

            try {
                Field mappedField = relatedEntity.getDeclaredField(oneToOne.mappedBy());
                return generateSelectQueryBuilder(relatedEntity).
                        addCondition(getTableName(relatedEntity),
                                getColumnNameFor(mappedField),
                                QueryBuilder.ConditionOperator.EQUALS).
                        toString();
            } catch (NoSuchFieldException e) {
                throw new MappingException("Field that mappedBy: " + oneToOne.mappedBy() +
                        " wasn't found", e);
            }
        }

        private Object getForeignKeyForOneToOne(OneToOne oneToOne, JoinColumn joinColumn,
                                                Class<?> originEntity, ResultSet resultSet)
                throws SQLException {
            if (oneToOne.mappedBy().equals("")) {
                String resultSetForeignKeyColumn = QueryBuilder.convertToResultSetColumn(
                        getTableName(originEntity), joinColumn.value());
                return resultSet.getObject(resultSetForeignKeyColumn);
            }

            String resultSetForeignKeyColumn = QueryBuilder.convertToResultSetColumn(
                    getTableName(originEntity), getIdColumnName(originEntity));
            return resultSet.getObject(resultSetForeignKeyColumn);
        }

        private Object findManyToOneValue(Field field, ResultSet resultSet) throws SQLException {
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (manyToOne.lazyInit()) {
                return null;
            }

            Class<?> relatedEntity = manyToOne.relatedEntity();
            String sqlQuery = generateSelectQueryBuilder(relatedEntity).
                    addCondition(getTableName(relatedEntity),
                            getIdColumnName(relatedEntity),
                            QueryBuilder.ConditionOperator.EQUALS).
                    toString();

            Class<?> originEntity = field.getDeclaringClass();
            String resultSetForeignKeyColumn = QueryBuilder.convertToResultSetColumn(
                    getTableName(originEntity), joinColumn.value());
            Object relatedIdValue = resultSet.getObject(resultSetForeignKeyColumn);

            GenericDao<Object, Object, ?> dao = new GenericDao<>(relatedEntity, connection);
            return dao.querySingle(sqlQuery, relatedIdValue).
                    orElseThrow(() -> new MappingException("Related entity wasn't found"));
        }

        private List<?> findOneToManyValues(Field field, ResultSet resultSet)
                throws SQLException {
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (oneToMany.lazyInit()) {
                return null;
            }

            try {
                Class<?> relatedEntity = oneToMany.relatedEntity();
                Field mappedField = relatedEntity.getDeclaredField(oneToMany.mappedBy());
                String sqlQuery = generateSelectQueryBuilder(relatedEntity).
                        addCondition(getTableName(relatedEntity),
                                getColumnNameFor(mappedField),
                                QueryBuilder.ConditionOperator.EQUALS).
                        toString();

                Class<?> originEntity = field.getDeclaringClass();
                String resultSetIdColumn = QueryBuilder.convertToResultSetColumn(
                        getTableName(originEntity), getIdColumnName(originEntity));
                Object idValue = resultSet.getObject(resultSetIdColumn);

                GenericDao<Object, Object, ?> dao = new GenericDao<>(relatedEntity, connection);
                return dao.queryList(sqlQuery, idValue);
            } catch (NoSuchFieldException e) {
                throw new MappingException("Field that mappedBy: " + oneToMany.mappedBy() +
                        " wasn't found", e);
            }
        }

        private Object findManyToManyValues(Field field, ResultSet resultSet)
                throws SQLException {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.lazyInit()) {
                return null;
            }

            Class<?> originEntity = field.getDeclaringClass();
            Class<?> relatedEntity = manyToMany.relatedEntity();

            String[] joinInfo = extractJoinInfoFrom(manyToMany);
            String sqlQuery = generateSelectQueryBuilder(relatedEntity).
                    addJoin(getTableName(relatedEntity), joinInfo[0],
                            getIdColumnName(relatedEntity), joinInfo[2]).
                    addJoin(joinInfo[0], getTableName(originEntity),
                            joinInfo[1], getIdColumnName(originEntity)).
                    addCondition(getTableName(originEntity), getIdColumnName(originEntity),
                            QueryBuilder.ConditionOperator.EQUALS).
                    toString();

            String resultSetIdColumn = QueryBuilder.convertToResultSetColumn(
                    getTableName(originEntity), getIdColumnName(originEntity));
            Object idValue = resultSet.getObject(resultSetIdColumn);

            GenericDao<Object, Object, ?> dao = new GenericDao<>(relatedEntity, connection);
            return dao.queryList(sqlQuery, idValue);
        }

        private String[] extractJoinInfoFrom(ManyToMany manyToMany) {
            System.out.println(manyToMany);
            JoinTable joinTable;
            if (!manyToMany.mappedBy().equals("")) {
                try {
                    Field field = manyToMany.relatedEntity().
                            getDeclaredField(manyToMany.mappedBy());
                    manyToMany = field.getAnnotation(ManyToMany.class);
                    joinTable = manyToMany.joinTable();
                    return new String[]{joinTable.name(), joinTable.inverseColumn(), joinTable.joinColumn()};
                } catch (NoSuchFieldException e) {
                    throw new MappingException("Field with name " +
                            manyToMany.mappedBy() + " wasn't found");
                }
            }
            joinTable = manyToMany.joinTable();
            return new String[]{joinTable.name(), joinTable.joinColumn(), joinTable.inverseColumn()};
        }
    }
}
