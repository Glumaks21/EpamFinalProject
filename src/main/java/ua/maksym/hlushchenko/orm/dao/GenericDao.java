package ua.maksym.hlushchenko.orm.dao;

import lombok.extern.slf4j.Slf4j;
import ua.maksym.hlushchenko.orm.entity.annotations.*;
import ua.maksym.hlushchenko.orm.exception.DaoException;
import ua.maksym.hlushchenko.orm.query.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static ua.maksym.hlushchenko.orm.entity.EntityParser.*;

@Slf4j
public class GenericDao<K, T> extends AbstractSqlDao<K, T> {
    private final Class<? extends T> entityClass;
    private final SqlMapper<T> mapper;

    public GenericDao(Class<? extends T> entityClass, Session session) {
        super(session instanceof CascadeDaoSession ?
                session :
                new CascadeDaoSession(session));
        this.entityClass = entityClass;

        mapper = new GenericMapper<>(entityClass, session);
    }

    @Override
    protected CascadeDaoSession getSession() {
        return (CascadeDaoSession) super.getSession();
    }

    @Override
    protected T mapEntity(ResultSet resultSet) {
        return mapper.map(resultSet);
    }

    @Override
    public List<T> findAll() {
        String query = GenericQueryCreator.createSelectAll(entityClass);
        return queryList(query);
    }

    @Override
    public Optional<T> find(K id) {
        String query = GenericQueryCreator.createSelectById(entityClass);
        return querySingle(query, id);
    }

    @Override
    public void save(T entity) {
        if (entity instanceof LoadProxy) {
            update(entity);
            return;
        }

        if (!isEntity(entity)) {
            throw new DaoException("Received object: " + entity + " is not an entity");
        }

        CascadeDaoSession session = getSession();
        if (session.isProcessed(entity)) {
            return;
        }

        if (session.containsTrace(entity)) {
            session.pushToTrace(entity);
            saveEntityInDb(entity);
            session.popFromTrace();
            return;
        }

        session.pushToTrace(entity);
        processCascadeEntity(entity, CascadeDaoSession::isCascadeSaveField,
                this::saveSingleRelatedField, this::saveCollectionRelatedField);
        if (!session.isProcessed(entity)) {
            saveEntityInDb(entity);
        }
        session.popFromTrace();
    }

    private void processCascadeEntity(T entity,
                                      Function<Field, Boolean> fieldChecker,
                                      BiConsumer<Field, T> singleRelatedProcessor,
                                      BiConsumer<Field, T> collectionRelatedProcessor) {
        getFullClassHierarchyOf(entityClass).stream().
                flatMap(aClass -> Arrays.stream(aClass.getDeclaredFields())).
                filter(fieldChecker::apply).
                forEach(field -> {
                    if (isSingleRelatedField(field)) {
                        singleRelatedProcessor.accept(field, entity);
                    } else if (isCollectionRelatedField(field)) {
                        collectionRelatedProcessor.accept(field, entity);
                    }
                });
    }

    private void saveSingleRelatedField(Field field, T entity) {
        Object relatedEntity = getValueOf(field, entity);
        if (relatedEntity != null) {
            ObjectDao<?, Object> dao = new GenericDao<>(field.getType(), getSession());
            dao.save(relatedEntity);
        }

        if (field.isAnnotationPresent(OneToOne.class)) {
            updateOneToOneLink(field, relatedEntity, entity);
        }
    }

    private void updateOneToOneLink(Field field, Object relatedEntity, T entity) {
        for (Class<?> scanClass : getFullClassHierarchyOf(entityClass)) {
            for (Field relatedEntityField : scanClass.getDeclaredFields()) {
                try {
                    if (relatedEntityField.get(relatedEntity) == entity) {
                        String table = getTableNameOf(entityClass);
                        String query = new UpdateQueryBuilder().
                                addMainTable(table).
                                addColumn(table, getColumnNameOf(field)).
                                addCondition(table, getIdColumnNameOf(entityClass), ConditionOperator.EQUALS).
                                toString();

                        getSession().update(query,
                                getIdValueOf(scanClass, relatedEntity),
                                getIdValueOf(entityClass, entity));
                    }
                } catch (IllegalAccessException e) {
                    throw new DaoException(e);
                }
            }
        }
    }

    private void saveCollectionRelatedField(Field field, T entity) {
        Collection<?> collection = (Collection<?>) getValueOf(field, entity);
        if (collection != null && !collection.isEmpty()) {
            Class<?> relatedEntityClass = field.isAnnotationPresent(OneToMany.class) ?
                    field.getAnnotation(OneToMany.class).genericType() :
                    field.getAnnotation(ManyToMany.class).genericType();

            ObjectDao<?, Object> dao = new GenericDao<>(relatedEntityClass, getSession());
            List<Object> firstSaved = new ArrayList<>();

            for (Object relatedEntity : collection) {
                if (!getSession().isProcessed(relatedEntity)) {
                    firstSaved.add(relatedEntity);
                }
                dao.save(relatedEntity);
            }

            if (field.isAnnotationPresent(ManyToMany.class)) {
                if (!getSession().isProcessed(entity)) {
                    save(entity);
                }
                saveCollectionInJoinTable(field, entity, firstSaved);
            }
        }
    }

    private void saveCollectionInJoinTable(Field field, T entity, Collection<?> collection) {
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        String[] params = QueryRelationUtil.getJoinTableValuesFor(field);
        String query = new InsertQueryBuilder().
                addMainTable(params[0]).
                addColumn(params[0], params[1]).
                addColumn(params[0], params[2]).
                toString();
        Object entityId = getIdValueOf(entityClass, entity);

        for (Object saved : collection) {
            Object savedId = getIdValueOf(manyToMany.genericType(), saved);
            getSession().update(query, entityId, savedId);
        }
    }

    private void saveEntityInDb(T entity) {
        List<Class<?>> hierarchy = getFullClassHierarchyOf(entityClass);
        for (int i = 0; i < hierarchy.size(); i++) {
            Class<?> currClass = hierarchy.get(i);

            if (i == hierarchy.size() - 1 || isTableClass(hierarchy.get(i + 1))) {
                saveInDbTable(currClass, entity);
            }
        }
    }

    private void saveInDbTable(Class<?> scanClass, Object entity) {
        try {
            String sqlQuery = GenericQueryCreator.createInsertQueryOf(scanClass);
            List<Object> args = getColumnValuesOf(scanClass, entity);

            for (Field field : getClassHierarchyByTableOf(scanClass).get(0).getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) && field.getAnnotation(Id.class).autoGenerated()) {
                    try (ResultSet resultSet = getSession().updateWithKeys(sqlQuery, args.toArray())) {
                        if (!resultSet.next()) {
                            throw new DaoException("Id wasn't generated");
                        }

                        Object generatedValue = resultSet.getObject(1, field.getType());
                        setValueTo(field, generatedValue, entity);
                    }
                    return;
                }
            }

            getSession().update(sqlQuery, args.toArray());
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
        CascadeDaoSession session = getSession();
        if (!loadProxy.isChanged() || session.containsTrace(entity)) {
            return;
        }

        session.pushToTrace(entity);
        processCascadeEntity(entity, CascadeDaoSession::isCascadeUpdateField,
                this::updateSingleRelatedField, this::updateCollectionRelatedField);
        if (!session.isProcessed(entity)) {
            updateEntityInDb(entity);
        }
        session.popFromTrace();
        loadProxy.setChanged(false);
    }

    private void updateSingleRelatedField(Field field, Object entity) {
        Object relatedEntity = getValueOf(field, entity);
        if (relatedEntity != null) {
            ObjectDao<?, Object> dao = new GenericDao<>(field.getType(), getSession());
            dao.update(relatedEntity);
        }
    }

    private void updateCollectionRelatedField(Field field, T entity) {
        Collection<?> collection = (Collection<?>) getValueOf(field, entity);
        if (collection != null && !collection.isEmpty()) {
            Class<?> relatedEntityClass = field.isAnnotationPresent(OneToMany.class) ?
                    field.getAnnotation(OneToMany.class).genericType() :
                    field.getAnnotation(ManyToMany.class).genericType();

            ObjectDao<?, Object> dao = new GenericDao<>(relatedEntityClass, getSession());
            collection.forEach(dao::update);
            if (field.isAnnotationPresent(ManyToMany.class)) {
                updateCollectionInJoinTable(field, entity, collection);
            }
        }
    }

    private void updateCollectionInJoinTable(Field field, T entity, Collection<?> collection) {
        boolean changed = collection.stream().
                filter(relatedEntity -> relatedEntity instanceof LoadProxy).
                map(relatedEntity -> (LoadProxy) relatedEntity).
                anyMatch(LoadProxy::isChanged);

        if (changed) {
            String[] params = QueryRelationUtil.getJoinTableValuesFor(field);
            String deleteQuery = new DeleteQueryBuilder().
                    addMainTable(params[0]).
                    addCondition(params[0], params[1], ConditionOperator.EQUALS).
                    toString();
            Object idValue = getIdValueOf(entityClass, entity);

            getSession().update(deleteQuery, idValue);
            saveCollectionInJoinTable(field, entity, collection);
        }
    }

    private void updateEntityInDb(T entity) {
        List<Class<?>> hierarchy = getFullClassHierarchyOf(entityClass);
        for (int i = 0; i < hierarchy.size(); i++) {
            Class<?> currClass = hierarchy.get(i);

            if (i == hierarchy.size() - 1 || isTableClass(hierarchy.get(i + 1))) {
                String sqlQuery = GenericQueryCreator.createUpdateByIdOf(currClass);
                List<Object> args = getColumnValuesOf(currClass, entity);
                args.add(getIdValueOf(currClass, entity));
                getSession().update(sqlQuery, args.toArray());
            }
        }
    }

    @Override
    public void delete(K id) {
        String query = GenericQueryCreator.createDeleteByIdQueryOf(entityClass);
        getSession().update(query, id);
    }

    @Override
    public void remove(T entity) {
        if (!(entity instanceof LoadProxy)) {
            return;
        }

        CascadeDaoSession session = getSession();
        if (session.containsTrace(entity)) {
            return;
        }

        session.pushToTrace(entity);
        processCascadeEntity(entity, CascadeDaoSession::isCascadeDeleteField,
                this::removeSingleRelatedField, this::removeCollectionRelatedField);
        if (!session.isProcessed(entity)) {
            deleteEntityInDb(entity);
        }
        session.popFromTrace();
    }

    private void removeSingleRelatedField(Field field, Object entity) {
        Object relatedEntity = getValueOf(field, entity);
        if (relatedEntity != null) {
            ObjectDao<?, Object> dao = new GenericDao<>(field.getType(), getSession());
            dao.remove(relatedEntity);
        }
    }

    private void removeCollectionRelatedField(Field field, T entity) {
        Collection<?> collection = (Collection<?>) getValueOf(field, entity);
        if (collection != null && !collection.isEmpty()) {
            Class<?> relatedEntityClass = field.isAnnotationPresent(OneToMany.class) ?
                    field.getAnnotation(OneToMany.class).genericType() :
                    field.getAnnotation(ManyToMany.class).genericType();

            ObjectDao<?, Object> dao = new GenericDao<>(relatedEntityClass, getSession());
            if (field.isAnnotationPresent(ManyToMany.class)) {
                String[] params = QueryRelationUtil.getJoinTableValuesFor(field);
                String deleteQuery = new DeleteQueryBuilder().
                        addMainTable(params[0]).
                        addCondition(params[0], params[1], ConditionOperator.EQUALS).
                        toString();
                Object idValue = getIdValueOf(entityClass, entity);

                getSession().update(deleteQuery, idValue);
            }
            collection.forEach(dao::remove);
        }
    }

    private void deleteEntityInDb(T entity) {
        List<Class<?>> hierarchy = getFullClassHierarchyOf(entityClass);
        for (int i = 0; i < hierarchy.size(); i++) {
            Class<?> currClass = hierarchy.get(i);

            if (i == hierarchy.size() - 1 || isTableClass(hierarchy.get(i + 1))) {
                String query = GenericQueryCreator.createDeleteByIdQueryOf(currClass);
                Object id = getIdValueOf(currClass, entity);
                getSession().update(query, id);
            }
        }
    }
}
