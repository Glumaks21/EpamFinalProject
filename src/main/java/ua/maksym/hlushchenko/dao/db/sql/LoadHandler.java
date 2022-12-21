package ua.maksym.hlushchenko.dao.db.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class LoadHandler<T> implements InvocationHandler {
    private final T wrapped;
    private boolean updated;

    public LoadHandler(T wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!updated && method.getName().startsWith("set")) {
            updated = true;
        } else if (method.getName().equals("isUpdated")) {
            return updated;
        }
        return method.invoke(wrapped, args);
    }
}
