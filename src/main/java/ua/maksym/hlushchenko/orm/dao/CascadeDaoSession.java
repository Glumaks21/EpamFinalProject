package ua.maksym.hlushchenko.orm.dao;

import ua.maksym.hlushchenko.orm.entity.annotations.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;

public class CascadeDaoSession implements Session {
    private final Session session;

    public final Set<Object> processed = new HashSet<>();
    public final List<Object> trace = new LinkedList<>();

    public CascadeDaoSession(Session session) {
        this.session = session;
    }

    public void pushToTrace(Object entity) {
        if (trace.isEmpty()) {
            command("SET FOREIGN_KEY_CHECKS = 0");
        }
        trace.add(entity);
    }

    public boolean containsTrace(Object entity) {
        return trace.contains(entity);
    }

    public boolean isProcessed(Object entity) {
        return processed.contains(entity);
    }

    public void popFromTrace() {
        processed.add(trace.remove(trace.size() - 1));
        if (trace.isEmpty()) {
            command("SET FOREIGN_KEY_CHECKS = 1");
            processed.clear();
        }
    }

    static boolean isCascadeSaveField(Field field) {
        CascadeType[] cascadeTypes = extractCascadeTypes(field);
        return cascadeTypes != null && Arrays.stream(cascadeTypes).
                anyMatch(type -> type == CascadeType.SAVE || type == CascadeType.ALL);
    }

    static boolean isCascadeUpdateField(Field field) {
        CascadeType[] cascadeTypes = extractCascadeTypes(field);
        return cascadeTypes != null && Arrays.stream(cascadeTypes).
                anyMatch(type -> type == CascadeType.UPDATE || type == CascadeType.ALL);
    }

    static boolean isCascadeDeleteField(Field field) {
        CascadeType[] cascadeTypes = extractCascadeTypes(field);
        return cascadeTypes != null && Arrays.stream(cascadeTypes).
                anyMatch(type -> type == CascadeType.DELETE || type == CascadeType.ALL);
    }

    private static CascadeType[] extractCascadeTypes(Field field) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            return field.getAnnotation(OneToOne.class).cascadeTypes();
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            return field.getAnnotation(ManyToOne.class).cascadeTypes();
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            return field.getAnnotation(OneToMany.class).cascadeTypes();
        } else if (field.isAnnotationPresent(ManyToMany.class)) {
            return field.getAnnotation(ManyToMany.class).cascadeTypes();
        }
        return null;
    }

    @Override
    public void command(String query) {
        session.command(query);
    }

    @Override
    public ResultSet query(String query, Object... args) {
        return session.query(query, args);
    }

    @Override
    public void update(String query, Object... args) {
        session.update(query, args);
    }

    @Override
    public ResultSet updateWithKeys(String query, Object... args) {
        return session.updateWithKeys(query, args);
    }

    @Override
    public void commit() {
        session.commit();
    }
}
