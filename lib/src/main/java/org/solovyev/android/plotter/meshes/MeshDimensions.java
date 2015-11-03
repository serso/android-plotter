package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

final class MeshDimensions {

    @Nonnull
    private Dimensions dimensions;

    MeshDimensions(@Nonnull Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public synchronized boolean set(@Nonnull Dimensions dimensions) {
        if (this.dimensions.equals(dimensions)) {
            return false;
        }

        this.dimensions = dimensions;
        return true;
    }

    @Nonnull
    public synchronized Dimensions get() {
        return dimensions;
    }
}
