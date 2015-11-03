package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;

public interface Group<M extends Mesh> extends Mesh, Iterable<M> {
    boolean add(@Nonnull M mesh);

    void clear();

    @Nonnull
    M get(int location);

    int size();

    @Nonnull
    M remove(int i);
}
