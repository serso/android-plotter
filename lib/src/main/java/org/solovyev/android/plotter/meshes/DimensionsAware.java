package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;

public interface DimensionsAware extends Mesh {

    @NonNull
    Dimensions getDimensions();

    void setDimensions(@NonNull Dimensions dimensions);
}
