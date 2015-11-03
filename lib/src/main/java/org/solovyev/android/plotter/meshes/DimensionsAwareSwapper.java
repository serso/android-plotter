package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

public final class DimensionsAwareSwapper implements DoubleBufferMesh.Swapper<DimensionsAware> {

    @NonNull
    public static final DoubleBufferMesh.Swapper<DimensionsAware> INSTANCE = new DimensionsAwareSwapper();

    private DimensionsAwareSwapper() {
    }

    @Override
    public void swap(@NonNull DimensionsAware current, @NonNull DimensionsAware next) {
        next.setColor(current.getColor());
        next.setWidth(current.getWidth());
        next.setDimensions(current.getDimensions());
    }
}
