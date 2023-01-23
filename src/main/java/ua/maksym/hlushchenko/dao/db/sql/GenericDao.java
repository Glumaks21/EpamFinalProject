package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.exception.*;
import ua.maksym.hlushchenko.orm.annotations.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import static ua.maksym.hlushchenko.dao.db.sql.EntityParser.*;
import static ua.maksym.hlushchenko.util.ReflectionUtil.*;

public class GenericDao<K, T> extends AbstractSqlDao<K, T> {
    private final String SQL_SELECT_ALL;
    private final String SQL_SELECT_BY_ID;

    private final Class<? extends T> entityClass;
    private final SqlMapper<T> mapper;

    private static final Logger log = LoggerFactory.getLogger(GenericDao.class);

    public GenericDao(Class<? extends T> clazz, Session session) {
        super(session);
        this.entityClass = clazz;

        mapper = new GenericMapper<>(clazz, session);

        SQL_SELECT_ALL = GenericQueryCreator.generateQueryByMethodName(clazz, "findAll");
        SQL_SELECT_BY_ID = GenericQueryCreator.generateQueryByMethodName(clazz, "find");
    }

    @Override
    protected T mapEntity(ResultSet resultSet) {
        return mapper.map(resultSet);
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
        if (entity instanceof LoadProxy) {
            update(entity);
            return;
        }

        for (Class<?> entityClass : getEntityHierarchyOf(entityClass)) {
            saveRelatedEntities(entityClass, entity);

            Dao<Object, Object> dao = new GenericDao<>(entityClass, session);
            dao.save(entity);
        }

        saveEntityInDb(entity);
    }

    private void saveRelatedEntities(Class<?> entityClass, Object entity) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToOne.class)) {
                OneToOne oneToOne = field.getAnnotation(OneToOne.class);

                CascadeType[] cascadeTypes = oneToOne.cascadeTypes();
                boolean containsSaveType = Arrays.stream(cascadeTypes).
                        anyMatch(type -> type == CascadeType.SAVE || type == CascadeType.ALL);
                if (containsSaveType) {
                    Dao<Object, Object> dao = new GenericDao<>(field.getType(), session);
                    Object value = getValueOf(field, entity);
                    dao.save(value);
                }
            }


        }
    }

    private void saveEntityInDb(Object entity) {
        try {
            String sqlQuery = GenericQueryCreator.createInsertQuery(entityClass, entity);

            List<Field> idFields = getDeclaredFieldsAnnotatedWith(entityClass, Id.class);
            if (!idFields.isEmpty()) {
                Field idField = idFields.get(0);

                Id id = idField.getAnnotation(Id.class);
                if (id.autoGenerated()) {
                    try (ResultSet resultSet = session.updateQueryWithKeys(sqlQuery)) {
                        if (resultSet.next()) {
                            Object generatedValue = resultSet.getObject(1, idField.getType());
                            setValueTo(idField, generatedValue, entity);
                        }
                    }
                    return;
                }
            }

            session.updateQuery(sqlQuery);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(T entity) {
        if (!(entity instanceof LoadProxy)) {
            save(entity);
            return;
        }

        LoadProxy loadProxy = (LoadProxy) entity;
        if (!loadProxy.isUpdated()) {
           return;
        }

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToOne.class)) {
                OneToOne oneToOne = field.getAnnotation(OneToOne.class);

                CascadeType[] cascadeTypes = oneToOne.cascadeTypes();
                boolean containsUpdateType = Arrays.stream(cascadeTypes).
                        anyMatch(type -> type == CascadeType.UPDATE || type == CascadeType.ALL);
                if (containsUpdateType) {
                    GenericDao<Object, Object> dao = new GenericDao<>(field.getType(), session);
                    dao.update(getValueOf(field, entity));
                }
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);

                CascadeType[] cascadeTypes = manyToOne.cascadeTypes();
                boolean containsUpdateType = Arrays.stream(cascadeTypes).
                        anyMatch(type -> type == CascadeType.UPDATE || type == CascadeType.ALL);
                if (containsUpdateType) {
                    GenericDao<Object, Object> dao = new GenericDao<>(field.getType(), session);
                    dao.update(getValueOf(field, entity));
                }
            }
        }

        String sqlQuery = GenericQueryCreator.createUpdateQuery(entityClass, entity);
        session.updateQuery(sqlQuery);
    }

    @Override
    public void delete(K id) {
        String sqlQuery = GenericQueryCreator.createDeleteQuery(entityClass, id);
        session.updateQuery(sqlQuery);
    }
}
