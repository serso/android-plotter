package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Function;

public interface FunctionGraph extends DimensionsAware {
    @NonNull
    Function getFunction();

    void setFunction(@NonNull Function function);
}
