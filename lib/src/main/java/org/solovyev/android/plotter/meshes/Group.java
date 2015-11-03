package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

public interface Group<M extends Mesh> extends Mesh, Iterable<M> {
    boolean add(@NonNull M mesh);

    void clear();

    @NonNull
    M get(int location);

    int size();

    @NonNull
    M remove(int i);
}
