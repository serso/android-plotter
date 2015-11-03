package org.solovyev.android.plotter;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.microedition.khronos.opengles.GL11;

/**
 * Contains information about functions to be plotted and meshes to be drawn. This class doesn't do plotting but
 * provides all required data to a {@link org.solovyev.android.plotter.PlottingView} which should be connected to it.
 * Note that {@link org.solovyev.android.plotter.PlottingView} might be attached and detached at any time and this
 * doesn't affect neither functions list nor any other plot data stored in this class. This class also makes sure that
 * all meshes (including functions' graphs) are initialized prior to draw.
 */
public interface Plotter {
    boolean D3 = true;

    void add(@NonNull Function function);

    void add(@NonNull PlotFunction function);

    void clearFunctions();

    void update(@NonNull PlotFunction function);

    void initGl(@NonNull GL11 gl, boolean firstTime);

    void draw(@NonNull GL11 gl, float labelsAlpha);

    @NonNull
    PlotData getPlotData();

    void attachView(@NonNull PlottingView view);

    void detachView(@NonNull PlottingView view);

    /**
     * @return a copy of dimensions
     */
    @NonNull
    Dimensions getDimensions();

    // should not be modified!
    @NonNull
    Dimensions.Scene getSceneDimensions();

    void updateScene(@Nullable Object source, @NonNull Zoom zoom, @NonNull RectSize viewSize, @NonNull RectSizeF sceneSize, @NonNull PointF sceneCenter);

    void updateGraph(@Nullable Object source, @NonNull RectSizeF graphSize, @NonNull PointF graphCenter);

    boolean is3d();

    void set3d(boolean d3);

    void showCoordinates(float x, float y);

    void hideCoordinates();

    void onCameraMoved(float dx, float dy);
}
