package ua.maksym.hlushchenko.orm.dao;

public interface LoadProxy {
    boolean isChanged();
    void setChanged(boolean state);
}
