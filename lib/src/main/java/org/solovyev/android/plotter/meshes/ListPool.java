package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class ListPool<M extends Mesh> implements Pool<M> {

    @NonNull
    private final List<M> list = new ArrayList<M>();
    @NonNull
    private final Callback<M> callback;

    public ListPool(@NonNull Callback<M> callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public M obtain() {
        final int poolSize = list.size();
        final M mesh;
        if (poolSize > 0) {
            mesh = list.remove(poolSize - 1);
        } else {
            mesh = callback.create();
        }
        return mesh;
    }

    @Override
    public void release(@NonNull M mesh) {
        callback.release(mesh);
        list.add(mesh);
    }

    @Override
    public void clear() {
        while (!list.isEmpty()) {
            callback.release(list.remove(list.size() - 1));
        }
    }

    public static interface Callback<M extends Mesh> {
        @NonNull
        M create();

        void release(@NonNull M mesh);
    }
}
