package org.solovyev.android.plotter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PlottingView {

    void requestRender();

    void zoom(boolean in);

    void resetZoom();

    void resetCamera();

    boolean removeCallbacks(@Nonnull Runnable runnable);

    boolean post(@Nonnull Runnable runnable);

    void set3d(boolean d3);

    void onDimensionChanged(@Nonnull Dimensions dimensions, @Nullable Object source);
}
