package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;

public interface Pool<M extends Mesh> {
    @Nonnull
    M obtain();

    void release(@Nonnull M mesh);

    void clear();
}
