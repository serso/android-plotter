package org.solovyev.android.drag;

public class MutableObject<T> {

    private volatile T object;

    public MutableObject() {
    }

    public MutableObject(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}

