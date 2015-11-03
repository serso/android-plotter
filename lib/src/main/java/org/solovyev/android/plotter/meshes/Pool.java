package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

public interface Pool<M extends Mesh> {
    @NonNull
    M obtain();

    void release(@NonNull M mesh);

    void clear();
}
