package org.solovyev.android.plotter.meshes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public final class ListPool<M extends Mesh> implements Pool<M> {

    @Nonnull
    private final List<M> list = new ArrayList<M>();
    @Nonnull
    private final Callback<M> callback;

    public ListPool(@Nonnull Callback<M> callback) {
        this.callback = callback;
    }

    @Nonnull
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
    public void release(@Nonnull M mesh) {
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
        @Nonnull
        M create();

        void release(@Nonnull M mesh);
    }
}
