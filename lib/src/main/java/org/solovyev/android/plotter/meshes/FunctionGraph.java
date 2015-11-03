package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public interface FunctionGraph extends DimensionsAware {
    @Nonnull
    Function getFunction();

    void setFunction(@Nonnull Function function);
}
