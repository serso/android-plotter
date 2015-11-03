package org.solovyev.android.plotter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface PlottingView {

    void requestRender();

    void zoom(boolean in);

    void resetZoom();

    void resetCamera();

    boolean removeCallbacks(@NonNull Runnable runnable);

    boolean post(@NonNull Runnable runnable);

    void set3d(boolean d3);

    void onDimensionChanged(@NonNull Dimensions dimensions, @Nullable Object source);
}
