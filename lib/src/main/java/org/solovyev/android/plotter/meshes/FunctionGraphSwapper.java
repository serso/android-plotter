package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

public final class FunctionGraphSwapper implements DoubleBufferMesh.Swapper<FunctionGraph> {

    @NonNull
    public static final DoubleBufferMesh.Swapper<FunctionGraph> INSTANCE = new FunctionGraphSwapper();

    private FunctionGraphSwapper() {
    }

    @Override
    public void swap(@NonNull FunctionGraph current, @NonNull FunctionGraph next) {
        DimensionsAwareSwapper.INSTANCE.swap(current, next);
        next.setFunction(current.getFunction());
        next.setPointsCount(current.getPointsCount());
    }
}
