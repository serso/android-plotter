package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;

final class MeshDimensions {

    @NonNull
    private Dimensions dimensions;

    MeshDimensions(@NonNull Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public synchronized boolean set(@NonNull Dimensions dimensions) {
        if (this.dimensions.equals(dimensions)) {
            return false;
        }

        this.dimensions = dimensions;
        return true;
    }

    @NonNull
    public synchronized Dimensions get() {
        return dimensions;
    }
}
