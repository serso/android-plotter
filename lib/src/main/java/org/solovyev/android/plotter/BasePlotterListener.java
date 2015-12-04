package org.solovyev.android.plotter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class BasePlotterListener implements Plotter.Listener {
    @Override
    public void onFunctionsChanged() {
    }

    @Override
    public void onFunctionAdded(@NonNull PlotFunction function) {
    }

    @Override
    public void onFunctionUpdated(int id, @NonNull PlotFunction function) {
    }

    @Override
    public void onFunctionRemoved(@NonNull PlotFunction function) {
    }

    @Override
    public void on3dChanged(boolean d3) {
    }

    @Override
    public void onDimensionsChanged(@Nullable Object source) {
    }

    @Override
    public void onViewAttached(@NonNull PlottingView view) {
    }

    @Override
    public void onViewDetached(@NonNull PlottingView view) {
    }
}
