package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.orm.annotations.Table;
import ua.maksym.hlushchenko.exception.EntityNotFoundException;

import java.io.*;
import java.util.*;

public class EntityManager {
    private final Map<String, Class<?>> entities = new HashMap<>();

    public void scanPackage(String packageName) {
        String packageFilePath = packageName.replaceAll("\\.", File.separator);
        try (InputStream stream = ClassLoader.getSystemResourceAsStream(packageFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            reader.lines().
                    filter(line -> line.endsWith(".class") &&
                            checkEntityClass(packageName + "." + trimExtension(line))).
                    forEach(line -> addEntity(trimExtension(line), packageName + "." + trimExtension(line)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String trimExtension(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    private boolean checkEntityClass(String classPath) {
        try {
            Class<?> clazz = Class.forName(classPath);
            return clazz.isAnnotationPresent(Table.class);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void addEntity(String name, String classPath) {
        try {
            addEntity(name, Class.forName(classPath));
        } catch (ClassNotFoundException e) {
            throw new EntityNotFoundException("Class is not found", e);
        }
    }

    public void addEntity(String name, Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new EntityNotFoundException("Class doesn't have an annotation");
        }
        entities.put(name, clazz);
    }

    public Class<?> getEntity(String simpleName) {
        if (!entities.containsKey(simpleName)) {
            throw new EntityNotFoundException("There is no entity mapping by name: " + simpleName);
        }
        return entities.get(simpleName);
    }

   public boolean isContainsEntity(Class<?> clazz) {
        return entities.containsValue(clazz);
   }
}

